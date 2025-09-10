package com.phuonghai.inspection.data.repository

import android.content.Context
import android.util.Log
import com.phuonghai.inspection.core.network.NetworkMonitor
import com.phuonghai.inspection.data.local.dao.LocalTaskDao
import com.phuonghai.inspection.data.local.entity.toDomainModel
import com.phuonghai.inspection.data.local.entity.toLocalEntity
import com.phuonghai.inspection.domain.model.Task
import com.phuonghai.inspection.domain.model.TaskStatus
import com.phuonghai.inspection.domain.repository.ITaskRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class OfflineTaskRepository @Inject constructor(
    private val localTaskDao: LocalTaskDao,
    private val onlineTaskRepository: TaskRepositoryImpl,
    private val networkMonitor: NetworkMonitor,
    @ApplicationContext private val context: Context
) : ITaskRepository {

    companion object {
        private const val TAG = "OfflineTaskRepository"
        private const val CACHE_EXPIRY_HOURS = 24
    }

    override suspend fun getTasksByInspectorId(inspectorId: String): Result<List<Task>> {
        return try {
            val isConnected = networkMonitor.isConnected.first()

            // Luôn lấy local data trước
            val localTasks = getLocalTasksForInspector(inspectorId)
            Log.d(TAG, "Found ${localTasks.size} local tasks for inspector: $inspectorId")

            if (isConnected) {
                // Online: Fetch và cache mới
                Log.d(TAG, "Online mode - fetching latest tasks")

                try {
                    val onlineResult = onlineTaskRepository.getTasksByInspectorId(inspectorId)

                    onlineResult.fold(
                        onSuccess = { onlineTasks ->
                            // Cache all tasks locally với timestamp
                            cacheTasksForInspector(inspectorId, onlineTasks)
                            Log.d(TAG, "Successfully cached ${onlineTasks.size} tasks")
                            Result.success(onlineTasks)
                        },
                        onFailure = { error ->
                            Log.w(TAG, "Online fetch failed, using cached data: ${error.message}")
                            // Return cached data if online fails
                            Result.success(localTasks)
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during online fetch: ${e.message}")
                    Result.success(localTasks)
                }
            } else {
                // Offline mode
                Log.d(TAG, "Offline mode - using cached tasks")

                if (localTasks.isEmpty()) {
                    Result.failure(Exception("Không có task nào được cache offline. Vui lòng kết nối internet để đồng bộ dữ liệu."))
                } else {
                    Result.success(localTasks)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getTasksByInspectorId", e)
            Result.failure(e)
        }
    }

    private suspend fun getLocalTasksForInspector(inspectorId: String): List<Task> {
        return try {
            val localEntities = localTaskDao.getTasksByInspectorIdSorted(inspectorId)
            localEntities.map { it.toDomainModel() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting local tasks", e)
            emptyList()
        }
    }

    private suspend fun cacheTasksForInspector(inspectorId: String, tasks: List<Task>) {
        try {
            val currentTime = System.currentTimeMillis()

            // Delete old cached tasks for this inspector
            localTaskDao.deleteTasksByInspectorId(inspectorId)

            // Insert new tasks with cache timestamp
            val entitiesToInsert = tasks.map { task ->
                task.toLocalEntity().copy(
                    cacheTimestamp = currentTime
                )
            }

            localTaskDao.insertTasks(entitiesToInsert)

            Log.d(TAG, "Cached ${tasks.size} tasks for inspector: $inspectorId")
        } catch (e: Exception) {
            Log.e(TAG, "Error caching tasks", e)
        }
    }

    override suspend fun getTask(taskId: String): Result<Task> {
        return try {
            // Try local first
            val localTask = localTaskDao.getTaskById(taskId)
            if (localTask != null && !isTaskCacheExpired(localTask.cacheTimestamp)) {
                Log.d(TAG, "Found task locally: $taskId")
                return Result.success(localTask.toDomainModel())
            }

            // If not found locally or expired, try online
            val isConnected = networkMonitor.isConnected.first()
            if (isConnected) {
                Log.d(TAG, "Fetching task online: $taskId")
                val result = onlineTaskRepository.getTask(taskId)

                // Cache the result if successful
                result.getOrNull()?.let { task ->
                    val entity = task.toLocalEntity().copy(
                        cacheTimestamp = System.currentTimeMillis()
                    )
                    localTaskDao.insertTask(entity)
                }

                result
            } else {
                if (localTask != null) {
                    Log.d(TAG, "Using expired cache for task: $taskId")
                    Result.success(localTask.toDomainModel())
                } else {
                    Result.failure(Exception("Task not found offline"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting task", e)
            Result.failure(e)
        }
    }

    private fun isTaskCacheExpired(cacheTimestamp: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val expiryTime = CACHE_EXPIRY_HOURS * 60 * 60 * 1000L
        return (currentTime - cacheTimestamp) > expiryTime
    }

    override suspend fun createTask(task: Task): Result<Unit> {
        return try {
            val isConnected = networkMonitor.isConnected.first()

            if (isConnected) {
                val result = onlineTaskRepository.createTask(task)
                if (result.isSuccess) {
                    // Cache locally
                    val entity = task.toLocalEntity().copy(
                        cacheTimestamp = System.currentTimeMillis()
                    )
                    localTaskDao.insertTask(entity)
                }
                result
            } else {
                // Store offline for later sync
                val entity = task.toLocalEntity().copy(
                    cacheTimestamp = System.currentTimeMillis(),
                    needsSync = true,
                    isLocalOnly = true
                )
                localTaskDao.insertTask(entity)
                Log.d(TAG, "Created task offline for later sync: ${task.taskId}")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating task", e)
            Result.failure(e)
        }
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit> {
        return try {
            val isConnected = networkMonitor.isConnected.first()

            // Always update local cache first
            val localTask = localTaskDao.getTaskById(taskId)
            if (localTask != null) {
                val updatedEntity = localTask.copy(
                    status = status.name,
                    needsSync = true,
                    localModifiedAt = System.currentTimeMillis()
                )
                localTaskDao.updateTask(updatedEntity)
            }

            if (isConnected) {
                // Try to sync immediately
                val result = onlineTaskRepository.updateTaskStatus(taskId, status)
                if (result.isSuccess && localTask != null) {
                    // Mark as synced
                    localTaskDao.markTaskAsSynced(taskId)
                }
                result
            } else {
                Log.d(TAG, "Updated task status offline: $taskId -> $status")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating task status", e)
            Result.failure(e)
        }
    }

    override suspend fun getTasksByBranch(branchId: String): Result<List<Task>> {
        return try {
            val isConnected = networkMonitor.isConnected.first()

            if (isConnected) {
                // Online: Fetch from server and cache
                Log.d(TAG, "Fetching tasks online for branch: $branchId")
                val result = onlineTaskRepository.getTasksByBranch(branchId)

                result.getOrNull()?.let { tasks ->
                    // Cache tasks locally
                    val entities = tasks.map { task ->
                        task.toLocalEntity().copy(
                            cacheTimestamp = System.currentTimeMillis()
                        )
                    }
                    entities.forEach { localTaskDao.insertTask(it) }
                }

                result
            } else {
                // Offline: Get from local database
                Log.d(TAG, "Getting tasks offline for branch: $branchId")
                val localTasks = localTaskDao.getTasksByBranchId(branchId)
                val domainTasks = localTasks.map { it.toDomainModel() }
                Result.success(domainTasks)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting tasks for branch: $branchId", e)
            Result.failure(e)
        }
    }

    override suspend fun getTaskIdByReportId(reportId: String): Result<String> {
        // This method relies on online data, delegate to online repository
        return onlineTaskRepository.getTaskIdByReportId(reportId)
    }

    // Additional offline-specific methods

    suspend fun syncTasksForInspector(inspectorId: String): Result<Int> {
        return try {
            val isConnected = networkMonitor.isConnected.first()

            if (!isConnected) {
                Log.w(TAG, "Cannot sync tasks while offline")
                return Result.failure(Exception("No internet connection"))
            }

            Log.d(TAG, "Syncing tasks for inspector: $inspectorId")

            // First sync unsynced local tasks
            val unsyncedTasks = localTaskDao.getUnsyncedTasksByInspector(inspectorId)
            var syncedCount = 0

            for (localTask in unsyncedTasks) {
                try {
                    val domainTask = localTask.toDomainModel()

                    if (localTask.isLocalOnly) {
                        // Create new task
                        val result = onlineTaskRepository.createTask(domainTask)
                        if (result.isSuccess) {
                            localTaskDao.markTaskAsSynced(localTask.taskId)
                            syncedCount++
                        }
                    } else {
                        // Update existing task
                        val result = onlineTaskRepository.updateTaskStatus(
                            localTask.taskId,
                            TaskStatus.valueOf(localTask.status)
                        )
                        if (result.isSuccess) {
                            localTaskDao.markTaskAsSynced(localTask.taskId)
                            syncedCount++
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync task: ${localTask.taskId}", e)
                    localTaskDao.updateSyncAttempt(localTask.taskId, System.currentTimeMillis())
                }
            }

            // Then fetch latest from server
            val onlineResult = onlineTaskRepository.getTasksByInspectorId(inspectorId)
            onlineResult.getOrNull()?.let { tasks ->
                cacheTasksForInspector(inspectorId, tasks)
            }

            Log.d(TAG, "Synced $syncedCount tasks for inspector: $inspectorId")
            Result.success(syncedCount)

        } catch (e: Exception) {
            Log.e(TAG, "Error syncing tasks", e)
            Result.failure(e)
        }
    }

    suspend fun getOfflineTasksCount(inspectorId: String): Int {
        return try {
            localTaskDao.getTasksCount(inspectorId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting offline tasks count", e)
            0
        }
    }

    suspend fun getUnsyncedTasksCount(inspectorId: String): Int {
        return try {
            localTaskDao.getUnsyncedTasksCountByInspector(inspectorId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unsynced tasks count", e)
            0
        }
    }

    suspend fun cleanupExpiredCache() {
        try {
            val expiryTime = System.currentTimeMillis() - (CACHE_EXPIRY_HOURS * 60 * 60 * 1000L)
            localTaskDao.deleteExpiredCache(expiryTime)
            Log.d(TAG, "Cleaned up expired task cache")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up cache", e)
        }
    }

    suspend fun cleanupOldTasks(olderThanDays: Int = 30) {
        try {
            val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
            localTaskDao.deleteOldTasks(cutoffTime)
            Log.d(TAG, "Cleaned up old tasks older than $olderThanDays days")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old tasks", e)
        }
    }

    // Statistics methods
    suspend fun getTaskStatistics(inspectorId: String): LocalTaskDao.TaskStatistics? {
        return try {
            localTaskDao.getTaskStatistics(inspectorId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting task statistics", e)
            null
        }
    }

    // Maintenance methods
    suspend fun trimOldSyncedTasks(inspectorId: String, keepCount: Int = 100) {
        try {
            localTaskDao.trimOldSyncedTasks(inspectorId, keepCount)
            Log.d(TAG, "Trimmed old synced tasks for inspector: $inspectorId")
        } catch (e: Exception) {
            Log.e(TAG, "Error trimming old tasks", e)
        }
    }
}