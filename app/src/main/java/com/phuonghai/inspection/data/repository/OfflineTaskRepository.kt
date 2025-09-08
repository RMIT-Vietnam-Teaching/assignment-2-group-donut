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
    }

    override suspend fun createTask(task: Task): Result<Unit> {
        return try {
            val isConnected = networkMonitor.isConnected.first()

            if (isConnected) {
                // Online: Create task directly
                Log.d(TAG, "Creating task online: ${task.title}")
                val result = onlineTaskRepository.createTask(task)

                if (result.isSuccess) {
                    // Also save locally for offline access
                    val localEntity = task.toLocalEntity()
                    localTaskDao.insertTask(localEntity)
                }

                result
            } else {
                // Offline: This should rarely happen for task creation
                // Tasks are usually created by supervisors, not inspectors
                Log.w(TAG, "Cannot create task offline: ${task.title}")
                Result.failure(Exception("Cannot create task while offline"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating task", e)
            Result.failure(e)
        }
    }

    override suspend fun getTask(taskId: String): Result<Task> {
        return try {
            // First try to get from local database
            val localTask = localTaskDao.getTaskById(taskId)
            if (localTask != null) {
                Log.d(TAG, "Found task locally: $taskId")
                return Result.success(localTask.toDomainModel())
            }

            // If not found locally and connected, try online
            val isConnected = networkMonitor.isConnected.first()
            if (isConnected) {
                Log.d(TAG, "Fetching task online: $taskId")
                val result = onlineTaskRepository.getTask(taskId)

                // Cache the result locally if successful
                result.getOrNull()?.let { task ->
                    val localEntity = task.toLocalEntity()
                    localTaskDao.insertTask(localEntity)
                }

                result
            } else {
                Log.d(TAG, "Task not found locally and no connection: $taskId")
                Result.failure(Exception("Task not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting task", e)
            Result.failure(e)
        }
    }

    override suspend fun getTasksByInspectorId(inspectorId: String): Result<List<Task>> {
        return try {
            val isConnected = networkMonitor.isConnected.first()

            if (isConnected) {
                // Online: Fetch latest tasks and cache them
                Log.d(TAG, "Fetching tasks online for inspector: $inspectorId")
                val onlineResult = onlineTaskRepository.getTasksByInspectorId(inspectorId)

                onlineResult.getOrNull()?.let { tasks ->
                    // Cache all tasks locally for offline access
                    val localEntities = tasks.map { it.toLocalEntity() }
                    localEntities.forEach { entity ->
                        localTaskDao.insertTask(entity)
                    }
                    Log.d(TAG, "Cached ${tasks.size} tasks locally")
                }

                onlineResult
            } else {
                // Offline: Return cached tasks
                Log.d(TAG, "Getting tasks offline for inspector: $inspectorId")
                val localTasks = localTaskDao.getTasksByInspectorIdSync(inspectorId)
                val domainTasks = localTasks.map { it.toDomainModel() }

                Log.d(TAG, "Found ${domainTasks.size} cached tasks for inspector: $inspectorId")
                Result.success(domainTasks)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting tasks for inspector: $inspectorId", e)
            Result.failure(e)
        }
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit> {
        return try {
            val isConnected = networkMonitor.isConnected.first()

            if (isConnected) {
                // Online: Update both online and locally
                Log.d(TAG, "Updating task status online: $taskId -> $status")
                val result = onlineTaskRepository.updateTaskStatus(taskId, status)

                if (result.isSuccess) {
                    // Update local copy as well
                    localTaskDao.updateTaskStatus(taskId, status.name)
                }

                result
            } else {
                // Offline: Update locally only
                Log.d(TAG, "Updating task status offline: $taskId -> $status")
                localTaskDao.updateTaskStatus(taskId, status.name)

                // TODO: Add to sync queue for later upload
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
                // Online: Fetch from server
                Log.d(TAG, "Fetching tasks online for branch: $branchId")
                onlineTaskRepository.getTasksByBranch(branchId)
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
            val onlineResult = onlineTaskRepository.getTasksByInspectorId(inspectorId)

            onlineResult.getOrNull()?.let { tasks ->
                // Replace all local tasks for this inspector
                val localEntities = tasks.map { it.toLocalEntity() }
                localEntities.forEach { entity ->
                    localTaskDao.insertTask(entity)
                }

                Log.d(TAG, "Synced ${tasks.size} tasks for inspector: $inspectorId")
                Result.success(tasks.size)
            } ?: Result.failure(Exception("Failed to fetch tasks from server"))

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

    suspend fun cleanupOldTasks(olderThanDays: Int = 30) {
        try {
            val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
            localTaskDao.deleteOldTasks(cutoffTime)
            Log.d(TAG, "Cleaned up old tasks older than $olderThanDays days")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old tasks", e)
        }
    }
}