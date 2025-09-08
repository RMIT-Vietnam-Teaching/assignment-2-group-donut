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
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncService: ReportSyncService,
    private val networkMonitor: NetworkMonitor
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting sync work")

            // Check if network is available
            val isConnected = networkMonitor.isConnected.first()
            if (!isConnected) {
                Log.d(TAG, "No network connection, skipping sync")
                return Result.retry()
            }

            // Check if there are reports to sync
            val unsyncedCount = syncService.getUnsyncedReportsCount()
            if (unsyncedCount == 0) {
                Log.d(TAG, "No reports to sync")
                return Result.success()
            }

            Log.d(TAG, "Found $unsyncedCount reports to sync")

            // Perform sync
            when (val syncResult = syncService.syncPendingReports()) {
                is SyncResult.Success -> {
                    Log.d(TAG, "Sync completed successfully. Synced: ${syncResult.syncedCount}, Failed: ${syncResult.failedCount}")

                    // Schedule next sync if there are still failed reports
                    if (syncResult.failedCount > 0) {
                        scheduleNextSync(applicationContext)
                    }

                    Result.success()
                }
                is SyncResult.Error -> {
                    Log.e(TAG, "Sync failed", syncResult.exception)
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in sync worker", e)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "SyncWorker"
        private const val SYNC_WORK_NAME = "report_sync_work"

        fun scheduleImmediateSync(context: Context) {
            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag("immediate_sync")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "immediate_$SYNC_WORK_NAME",
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )

            Log.d(TAG, "Scheduled immediate sync")
        }

        fun schedulePeriodicSync(context: Context) {
            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag("periodic_sync")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )

            Log.d(TAG, "Scheduled periodic sync")
        }

        private fun scheduleNextSync(context: Context) {
            val retryRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInitialDelay(30, TimeUnit.MINUTES) // Retry after 30 minutes
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag("retry_sync")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "retry_$SYNC_WORK_NAME",
                ExistingWorkPolicy.REPLACE,
                retryRequest
            )

            Log.d(TAG, "Scheduled retry sync in 30 minutes")
        }

        fun cancelAllSync(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag("immediate_sync")
            WorkManager.getInstance(context).cancelAllWorkByTag("periodic_sync")
            WorkManager.getInstance(context).cancelAllWorkByTag("retry_sync")
            WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
            Log.d(TAG, "Cancelled all sync work")
        }
    }
}