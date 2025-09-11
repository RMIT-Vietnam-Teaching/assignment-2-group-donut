package com.phuonghai.inspection.core.sync

import android.net.Uri
import android.util.Log
import com.phuonghai.inspection.core.network.NetworkMonitor
import com.phuonghai.inspection.core.storage.OfflineFileManager
import com.phuonghai.inspection.data.local.dao.LocalReportDao
import com.phuonghai.inspection.data.local.entity.LocalReportEntity
import com.phuonghai.inspection.data.local.entity.toDomainModel
import com.phuonghai.inspection.domain.repository.IReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportSyncService @Inject constructor(
    private val localReportDao: LocalReportDao,
    private val reportRepository: IReportRepository,
    private val networkMonitor: NetworkMonitor,
    private val fileManager: OfflineFileManager
) {

    companion object {
        private const val TAG = "ReportSyncService"
        private const val MAX_RETRY_COUNT = 5
        private const val RETRY_DELAY_MS = 30000L // 30 seconds
    }

    private val _syncProgress = MutableStateFlow<SyncProgress>(SyncProgress.Idle)
    val syncProgress: StateFlow<SyncProgress> = _syncProgress.asStateFlow()

    suspend fun syncAllPendingReports(): SyncResult {
        try {
            val isConnected = networkMonitor.isConnected.first()
            if (!isConnected) {
                Log.w(TAG, "Cannot sync - no internet connection")
                return SyncResult.Error(Exception("No internet connection"))
            }

            Log.d(TAG, "Starting comprehensive report sync")
            _syncProgress.value = SyncProgress.InProgress(0, 0)

            // Get all unsynced reports
            val unsyncedReports = localReportDao.getReportsForSync()
            Log.d(TAG, "Found ${unsyncedReports.size} reports to sync")

            if (unsyncedReports.isEmpty()) {
                _syncProgress.value = SyncProgress.Completed(0, 0, 0)
                return SyncResult.Success(0, 0)
            }

            var successCount = 0
            var failureCount = 0

            for ((index, localReport) in unsyncedReports.withIndex()) {
                _syncProgress.value = SyncProgress.InProgress(index + 1, unsyncedReports.size)

                try {
                    val syncResult = syncSingleReport(localReport)
                    if (syncResult) {
                        successCount++
                        Log.d(TAG, "Successfully synced report: ${localReport.reportId}")
                    } else {
                        failureCount++
                        handleSyncFailure(localReport)
                    }
                } catch (e: Exception) {
                    failureCount++
                    Log.e(TAG, "Exception syncing report: ${localReport.reportId}", e)
                    handleSyncFailure(localReport)
                }
            }

            _syncProgress.value = SyncProgress.Completed(successCount, failureCount, unsyncedReports.size)
            Log.d(TAG, "Sync completed. Success: $successCount, Failed: $failureCount")

            return SyncResult.Success(successCount, failureCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error during sync process", e)
            _syncProgress.value = SyncProgress.Error(e.message ?: "Unknown error")
            return SyncResult.Error(e)
        }
    }

    private suspend fun syncSingleReport(localReport: LocalReportEntity): Boolean {
        return try {
            Log.d(TAG, "Syncing report: ${localReport.reportId}")

            // Step 1: Upload media files if they exist locally
            var finalImageUrl = localReport.imageUrl
            var finalVideoUrl = localReport.videoUrl

            // Upload local image if exists
            if (localReport.localImagePath.isNotBlank()) {
                val uploadedImageUrl = uploadLocalImage(localReport.localImagePath)
                if (uploadedImageUrl != null) {
                    finalImageUrl = uploadedImageUrl
                    Log.d(TAG, "Uploaded local image: $uploadedImageUrl")
                } else {
                    Log.w(TAG, "Failed to upload local image: ${localReport.localImagePath}")
                    // Don't fail the entire sync for image upload failure
                }
            }

            // Upload local video if exists
            if (localReport.localVideoPath.isNotBlank()) {
                val uploadedVideoUrl = uploadLocalVideo(localReport.localVideoPath)
                if (uploadedVideoUrl != null) {
                    finalVideoUrl = uploadedVideoUrl
                    Log.d(TAG, "Uploaded local video: $uploadedVideoUrl")
                } else {
                    Log.w(TAG, "Failed to upload local video: ${localReport.localVideoPath}")
                    // Don't fail the entire sync for video upload failure
                }
            }

            // Step 2: Create/Update the report with final URLs
            val updatedReport = localReport.copy(
                imageUrl = finalImageUrl,
                videoUrl = finalVideoUrl,
                syncStatus = "SYNCED",
                needsSync = false
            )

            val domainReport = updatedReport.toDomainModel()
            val result = reportRepository.createReport(domainReport)

            result.fold(
                onSuccess = {
                    // Mark as synced and cleanup
                    localReportDao.markAsSynced(localReport.reportId)
                    localReportDao.resetSyncRetryCount(localReport.reportId)
                    cleanupLocalFiles(localReport)
                    true
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to sync report to Firebase: ${localReport.reportId}", error)
                    false
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception in syncSingleReport: ${localReport.reportId}", e)
            false
        }
    }

    private suspend fun uploadLocalImage(localImagePath: String): String? {
        return try {
            if (localImagePath.isBlank()) return null

            val localFile = fileManager.getLocalFile(localImagePath)
            if (localFile?.exists() != true) {
                Log.w(TAG, "Local image file not found: $localImagePath")
                return null
            }

            Log.d(TAG, "Uploading local image: $localImagePath")
            val uri = Uri.fromFile(localFile)
            val result = reportRepository.uploadImage(uri)

            result.getOrElse {
                Log.e(TAG, "Failed to upload image: ${it.message}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception uploading local image", e)
            null
        }
    }

    private suspend fun uploadLocalVideo(localVideoPath: String): String? {
        return try {
            if (localVideoPath.isBlank()) return null

            val localFile = fileManager.getLocalFile(localVideoPath)
            if (localFile?.exists() != true) {
                Log.w(TAG, "Local video file not found: $localVideoPath")
                return null
            }

            Log.d(TAG, "Uploading local video: $localVideoPath")
            val uri = Uri.fromFile(localFile)
            val result = reportRepository.uploadVideo(uri)

            result.getOrElse {
                Log.e(TAG, "Failed to upload video: ${it.message}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception uploading local video", e)
            null
        }
    }

    private suspend fun handleSyncFailure(localReport: LocalReportEntity) {
        try {
            val newRetryCount = localReport.syncRetryCount + 1

            if (newRetryCount >= MAX_RETRY_COUNT) {
                Log.w(TAG, "Report ${localReport.reportId} exceeded max retry count, marking as failed")
                // Could mark as permanently failed or queue for manual review
            }

            localReportDao.updateSyncAttempt(localReport.reportId, System.currentTimeMillis())
        } catch (e: Exception) {
            Log.e(TAG, "Error handling sync failure", e)
        }
    }

    private suspend fun cleanupLocalFiles(localReport: LocalReportEntity) {
        try {
            if (localReport.localImagePath.isNotBlank()) {
                fileManager.deleteLocalFile(localReport.localImagePath)
                Log.d(TAG, "Cleaned up local image: ${localReport.localImagePath}")
            }
            if (localReport.localVideoPath.isNotBlank()) {
                fileManager.deleteLocalFile(localReport.localVideoPath)
                Log.d(TAG, "Cleaned up local video: ${localReport.localVideoPath}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up local files for report: ${localReport.reportId}", e)
        }
    }

    suspend fun getUnsyncedReportsCount(): Int {
        return try {
            localReportDao.getUnsyncedReportsCount()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unsynced reports count", e)
            0
        }
    }

    suspend fun getSyncStatus(): SyncStatus {
        return try {
            val unsyncedCount = getUnsyncedReportsCount()
            val allUnsynced = localReportDao.getUnsyncedReports()
            val failedReports = allUnsynced.filter { it.syncRetryCount >= MAX_RETRY_COUNT }

            SyncStatus(
                totalUnsynced = unsyncedCount,
                failedReports = failedReports.size,
                lastSyncAttempt = allUnsynced.maxByOrNull { it.lastSyncAttempt }?.lastSyncAttempt ?: 0L
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting sync status", e)
            SyncStatus(0, 0, 0L)
        }
    }

    // Auto-sync when network becomes available
    // Alias method for backward compatibility
    suspend fun syncPendingReports(): SyncResult {
        return syncAllPendingReports()
    }

    suspend fun scheduleAutoSync() {
        networkMonitor.isConnected.collect { isConnected ->
            if (isConnected) {
                val unsyncedCount = getUnsyncedReportsCount()
                if (unsyncedCount > 0) {
                    Log.d(TAG, "Network restored - auto-syncing $unsyncedCount reports")
                    syncAllPendingReports()
                }
            }
        }
    }

    // Sync specific report by ID
    suspend fun syncReportById(reportId: String): Result<Unit> {
        return try {
            val localReport = localReportDao.getReportById(reportId)
            if (localReport == null) {
                return Result.failure(Exception("Report not found locally: $reportId"))
            }

            val isConnected = networkMonitor.isConnected.first()
            if (!isConnected) {
                return Result.failure(Exception("No internet connection"))
            }

            val success = syncSingleReport(localReport)
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync report"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing specific report: $reportId", e)
            Result.failure(e)
        }
    }

    // Force retry failed reports
    suspend fun retryFailedReports(): SyncResult {
        return try {
            val failedReports = localReportDao.getUnsyncedReports()
                .filter { it.syncRetryCount >= MAX_RETRY_COUNT }

            if (failedReports.isEmpty()) {
                return SyncResult.Success(0, 0)
            }

            Log.d(TAG, "Retrying ${failedReports.size} failed reports")

            var successCount = 0
            var failureCount = 0

            for (report in failedReports) {
                // Reset retry count for another attempt
                localReportDao.resetSyncRetryCount(report.reportId)

                try {
                    val success = syncSingleReport(report)
                    if (success) {
                        successCount++
                    } else {
                        failureCount++
                        handleSyncFailure(report)
                    }
                } catch (e: Exception) {
                    failureCount++
                    handleSyncFailure(report)
                }
            }

            SyncResult.Success(successCount, failureCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrying failed reports", e)
            SyncResult.Error(e)
        }
    }
}

sealed class SyncProgress {
    object Idle : SyncProgress()
    data class InProgress(val current: Int, val total: Int) : SyncProgress()
    data class Completed(val success: Int, val failed: Int, val total: Int) : SyncProgress()
    data class Error(val message: String) : SyncProgress()
}

sealed class SyncResult {
    data class Success(val syncedCount: Int, val failedCount: Int) : SyncResult()
    data class Error(val exception: Throwable) : SyncResult()
}

data class SyncStatus(
    val totalUnsynced: Int,
    val failedReports: Int,
    val lastSyncAttempt: Long
)