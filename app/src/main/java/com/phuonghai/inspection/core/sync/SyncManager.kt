package com.phuonghai.inspection.core.sync

import android.content.Context
import android.util.Log
import com.phuonghai.inspection.core.network.NetworkMonitor
import com.phuonghai.inspection.domain.repository.IAuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkMonitor: NetworkMonitor,
    private val authRepository: IAuthRepository,
    private val taskSyncService: TaskSyncService,
    private val reportSyncService: ReportSyncService
) {

    companion object {
        private const val TAG = "SyncManager"
    }

    fun initialize() {
        Log.d(TAG, "Initializing SyncManager")

        // Schedule periodic syncing
        schedulePeriodicSync()

        Log.d(TAG, "SyncManager initialized")
    }

    private fun schedulePeriodicSync() {
        // Schedule periodic background sync using WorkManager
        TaskSyncWorker.schedulePeriodicSync(context)
        SyncWorker.schedulePeriodicSync(context) // For reports

        Log.d(TAG, "Periodic sync scheduled")
    }

    suspend fun performFullSync(): FullSyncResult {
        return try {
            Log.d(TAG, "Starting full sync")

            // Check authentication
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                Log.w(TAG, "No authenticated user for sync")
                return FullSyncResult.Error("No authenticated user")
            }

            // Check network
            val isConnected = networkMonitor.isConnected.first()
            if (!isConnected) {
                Log.w(TAG, "No network connection for sync")
                return FullSyncResult.Error("No network connection")
            }

            var tasksSync = 0
            var reportsSync = 0
            val errors = mutableListOf<String>()

            // Sync tasks
            when (val taskResult = taskSyncService.autoSyncTasks()) {
                is TaskSyncResult.Success -> {
                    tasksSync = taskResult.taskCount
                    Log.d(TAG, "Tasks synced: $tasksSync")
                }
                is TaskSyncResult.Error -> {
                    errors.add("Task sync failed: ${taskResult.exception.message}")
                }
            }

            // Sync reports - FIXED: Use syncAllPendingReports instead
            when (val reportResult = reportSyncService.syncAllPendingReports()) {
                is com.phuonghai.inspection.core.sync.SyncResult.Success -> {
                    reportsSync = reportResult.syncedCount
                    Log.d(TAG, "Reports synced: $reportsSync")
                }
                is com.phuonghai.inspection.core.sync.SyncResult.Error -> {
                    errors.add("Report sync failed: ${reportResult.exception.message}")
                }
            }

            if (errors.isEmpty()) {
                FullSyncResult.Success(tasksSync, reportsSync)
            } else {
                FullSyncResult.PartialSuccess(tasksSync, reportsSync, errors)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error during full sync", e)
            FullSyncResult.Error(e.message ?: "Unknown error")
        }
    }

    fun scheduleImmediateSync() {
        Log.d(TAG, "Scheduling immediate sync")
        TaskSyncWorker.scheduleImmediateSync(context)
        SyncWorker.scheduleImmediateSync(context)
    }

    suspend fun getOfflineDataInfo(): OfflineDataInfo {
        return try {
            val taskInfo = taskSyncService.getOfflineTasksInfo()
            val reportInfo = reportSyncService.getUnsyncedReportsCount()

            OfflineDataInfo(
                totalTasks = taskInfo.totalTasks,
                unsyncedReports = reportInfo,
                hasOfflineData = taskInfo.isAvailable || reportInfo > 0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting offline data info", e)
            OfflineDataInfo(0, 0, false)
        }
    }

    fun cleanup() {
        Log.d(TAG, "Cleaning up SyncManager")
        TaskSyncWorker.cancelAllSyncWork(context)
        // SyncWorker.cancelAllSyncWork(context) // If you implement this
    }
}

sealed class FullSyncResult {
    data class Success(val tasksSynced: Int, val reportsSynced: Int) : FullSyncResult()
    data class PartialSuccess(
        val tasksSynced: Int,
        val reportsSynced: Int,
        val errors: List<String>
    ) : FullSyncResult()
    data class Error(val message: String) : FullSyncResult()
}

data class OfflineDataInfo(
    val totalTasks: Int,
    val unsyncedReports: Int,
    val hasOfflineData: Boolean
)