package com.phuonghai.inspection.core.sync

import android.net.Uri
import android.util.Log
import com.phuonghai.inspection.core.storage.OfflineFileManager
import com.phuonghai.inspection.data.local.dao.LocalReportDao
import com.phuonghai.inspection.data.local.entity.LocalReportEntity
import com.phuonghai.inspection.data.local.entity.toDomainModel
import com.phuonghai.inspection.domain.repository.IReportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportSyncService @Inject constructor(
    private val localReportDao: LocalReportDao,
    private val reportRepository: IReportRepository,
    private val fileManager: OfflineFileManager
) {

    private val _syncProgress = MutableStateFlow<SyncProgress>(SyncProgress.Idle)
    val syncProgress: StateFlow<SyncProgress> = _syncProgress.asStateFlow()

    suspend fun syncPendingReports(): SyncResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting sync of pending reports")
            _syncProgress.value = SyncProgress.InProgress(0, 0)

            val unsyncedReports = localReportDao.getReportsForSync()
            Log.d(TAG, "Found ${unsyncedReports.size} reports to sync")

            if (unsyncedReports.isEmpty()) {
                _syncProgress.value = SyncProgress.Completed(0, 0, 0)
                return@withContext SyncResult.Success(0, 0)
            }

            var successCount = 0
            var failureCount = 0

            unsyncedReports.forEachIndexed { index, localReport ->
                _syncProgress.value = SyncProgress.InProgress(index + 1, unsyncedReports.size)

                try {
                    Log.d(TAG, "Syncing report: ${localReport.reportId}")

                    // Upload media files first if they exist locally
                    val uploadedImageUrl = uploadLocalImageIfExists(localReport)
                    val uploadedVideoUrl = uploadLocalVideoIfExists(localReport)

                    // Update report with uploaded URLs
                    val updatedReport = localReport.copy(
                        imageUrl = uploadedImageUrl ?: localReport.imageUrl,
                        videoUrl = uploadedVideoUrl ?: localReport.videoUrl,
                        syncStatus = "SYNCED"
                    )

                    // Convert to domain model and sync to Firebase
                    val domainReport = updatedReport.toDomainModel()
                    val result = reportRepository.createReport(domainReport)

                    result.fold(
                        onSuccess = { remoteId ->
                            val syncedEntity = updatedReport.copy(
                                reportId = remoteId,
                                needsSync = false
                            )
                            localReportDao.deleteReportById(localReport.reportId)
                            localReportDao.insertReport(syncedEntity)
                            localReportDao.resetSyncRetryCount(remoteId)

                            // Clean up local media files after successful sync
                            cleanupLocalFiles(localReport)

                            successCount++
                            Log.d(TAG, "Successfully synced report: ${localReport.reportId}")
                        },
                        onFailure = { error ->
                            // Update sync attempt
                            localReportDao.updateSyncAttempt(localReport.reportId, System.currentTimeMillis())
                            failureCount++
                            Log.e(TAG, "Failed to sync report: ${localReport.reportId}", error)
                        }
                    )
                } catch (e: Exception) {
                    localReportDao.updateSyncAttempt(localReport.reportId, System.currentTimeMillis())
                    failureCount++
                    Log.e(TAG, "Exception syncing report: ${localReport.reportId}", e)
                }
            }

            _syncProgress.value = SyncProgress.Completed(successCount, failureCount, unsyncedReports.size)
            Log.d(TAG, "Sync completed. Success: $successCount, Failed: $failureCount")

            SyncResult.Success(successCount, failureCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error during sync process", e)
            _syncProgress.value = SyncProgress.Error(e.message ?: "Unknown error")
            SyncResult.Error(e)
        }
    }

    private suspend fun uploadLocalImageIfExists(localReport: LocalReportEntity): String? {
        if (localReport.localImagePath.isNotBlank()) {
            val localFile = fileManager.getLocalFile(localReport.localImagePath)
            if (localFile?.exists() == true) {
                Log.d(TAG, "Uploading local image: ${localReport.localImagePath}")
                val uri = Uri.fromFile(localFile)
                val result = reportRepository.uploadImage(uri)
                return result.getOrNull()
            }
        }
        return null
    }

    private suspend fun uploadLocalVideoIfExists(localReport: LocalReportEntity): String? {
        if (localReport.localVideoPath.isNotBlank()) {
            val localFile = fileManager.getLocalFile(localReport.localVideoPath)
            if (localFile?.exists() == true) {
                Log.d(TAG, "Uploading local video: ${localReport.localVideoPath}")
                val uri = Uri.fromFile(localFile)
                val result = reportRepository.uploadVideo(uri)
                return result.getOrNull()
            }
        }
        return null
    }

    private suspend fun cleanupLocalFiles(localReport: LocalReportEntity) {
        try {
            if (localReport.localImagePath.isNotBlank()) {
                fileManager.deleteLocalFile(localReport.localImagePath)
            }
            if (localReport.localVideoPath.isNotBlank()) {
                fileManager.deleteLocalFile(localReport.localVideoPath)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up local files for report: ${localReport.reportId}", e)
        }
    }

    suspend fun getUnsyncedReportsCount(): Int {
        return localReportDao.getUnsyncedReportsCount()
    }

    suspend fun getSyncStatus(): SyncStatus {
        val unsyncedCount = getUnsyncedReportsCount()
        val allReports = localReportDao.getUnsyncedReports()
        val failedReports = allReports.filter { it.syncRetryCount >= 3 }

        return SyncStatus(
            totalUnsynced = unsyncedCount,
            failedReports = failedReports.size,
            lastSyncAttempt = allReports.maxByOrNull { it.lastSyncAttempt }?.lastSyncAttempt ?: 0L
        )
    }

    companion object {
        private const val TAG = "ReportSyncService"
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