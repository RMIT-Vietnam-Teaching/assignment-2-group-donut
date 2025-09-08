package com.phuonghai.inspection.core.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.phuonghai.inspection.core.network.NetworkMonitor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class TaskSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskSyncService: TaskSyncService,
    private val networkMonitor: NetworkMonitor
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "TaskSyncWorker"
        const val WORK_NAME = "task_sync_work"
        const val PERIODIC_WORK_NAME = "periodic_task_sync_work"

        fun scheduleImmediateSync(context: Context) {
            Log.d(TAG, "Scheduling immediate task sync")

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<TaskSyncWorker>()
                .setConstraints(constraints)
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    syncRequest
                )
        }

        fun schedulePeriodicSync(context: Context) {
            Log.d(TAG, "Scheduling periodic task sync")

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val periodicSyncRequest = PeriodicWorkRequestBuilder<TaskSyncWorker>(
                repeatInterval = 6, // Every 6 hours
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeInterval = 1, // With 1 hour flex
                flexTimeIntervalUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .addTag(PERIODIC_WORK_NAME)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    PERIODIC_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicSyncRequest
                )
        }

        fun cancelAllSyncWork(context: Context) {
            Log.d(TAG, "Cancelling all task sync work")
            WorkManager.getInstance(context)
                .cancelUniqueWork(WORK_NAME)
            WorkManager.getInstance(context)
                .cancelUniqueWork(PERIODIC_WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Task sync work started")

            // Check if we have network connection
            val isConnected = networkMonitor.isConnected.first()
            if (!isConnected) {
                Log.w(TAG, "No network connection, skipping sync")
                return Result.retry()
            }

            // Perform the sync
            when (val syncResult = taskSyncService.autoSyncTasks()) {
                is TaskSyncResult.Success -> {
                    Log.d(TAG, "Task sync completed successfully: ${syncResult.taskCount} tasks")
                    Result.success()
                }
                is TaskSyncResult.Error -> {
                    Log.e(TAG, "Task sync failed", syncResult.exception)
                    // Retry with exponential backoff
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during task sync", e)
            Result.failure()
        }
    }
}