package com.phuonghai.inspection.core.sync

import android.util.Log
import com.phuonghai.inspection.core.network.NetworkMonitor
import com.phuonghai.inspection.data.repository.OfflineTaskRepository
import com.phuonghai.inspection.domain.repository.IAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskSyncService @Inject constructor(
    private val offlineTaskRepository: OfflineTaskRepository,
    private val authRepository: IAuthRepository,
    private val networkMonitor: NetworkMonitor
) {

    private val _syncProgress = MutableStateFlow<TaskSyncProgress>(TaskSyncProgress.Idle)
    val syncProgress: StateFlow<TaskSyncProgress> = _syncProgress.asStateFlow()

    suspend fun autoSyncTasks(): TaskSyncResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting auto sync of tasks")
            _syncProgress.value = TaskSyncProgress.InProgress

            // Check if online
            val isConnected = networkMonitor.isConnected.first()
            if (!isConnected) {
                Log.w(TAG, "Cannot sync tasks - no internet connection")
                _syncProgress.value = TaskSyncProgress.Error("No internet connection")
                return@withContext TaskSyncResult.Error(Exception("No internet connection"))
            }

            // Get current user
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                Log.w(TAG, "Cannot sync tasks - no authenticated user")
                _syncProgress.value = TaskSyncProgress.Error("No authenticated user")
                return@withContext TaskSyncResult.Error(Exception("No authenticated user"))
            }

            // Sync tasks for current inspector
            val syncResult = offlineTaskRepository.syncTasksForInspector(currentUser.uId)

            syncResult.fold(
                onSuccess = { taskCount ->
                    Log.d(TAG, "Successfully synced $taskCount tasks")
                    _syncProgress.value = TaskSyncProgress.Completed(taskCount)
                    TaskSyncResult.Success(taskCount)
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to sync tasks", exception)
                    _syncProgress.value = TaskSyncProgress.Error(exception.message ?: "Unknown error")
                    TaskSyncResult.Error(exception)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error during task sync", e)
            _syncProgress.value = TaskSyncProgress.Error(e.message ?: "Unknown error")
            TaskSyncResult.Error(e)
        }
    }

    suspend fun schedulePeriodicSync() {
        // This method can be called when app starts or network becomes available
        try {
            val isConnected = networkMonitor.isConnected.first()
            if (isConnected) {
                autoSyncTasks()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling periodic sync", e)
        }
    }

    suspend fun getOfflineTasksInfo(): OfflineTasksInfo {
        return try {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                val taskCount = offlineTaskRepository.getOfflineTasksCount(currentUser.uId)
                OfflineTasksInfo(
                    totalTasks = taskCount,
                    isAvailable = taskCount > 0
                )
            } else {
                OfflineTasksInfo(0, false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting offline tasks info", e)
            OfflineTasksInfo(0, false)
        }
    }

    companion object {
        private const val TAG = "TaskSyncService"
    }
}

sealed class TaskSyncProgress {
    object Idle : TaskSyncProgress()
    object InProgress : TaskSyncProgress()
    data class Completed(val taskCount: Int) : TaskSyncProgress()
    data class Error(val message: String) : TaskSyncProgress()
}

sealed class TaskSyncResult {
    data class Success(val taskCount: Int) : TaskSyncResult()
    data class Error(val exception: Throwable) : TaskSyncResult()
}

data class OfflineTasksInfo(
    val totalTasks: Int,
    val isAvailable: Boolean
)