package com.phuonghai.inspection.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
import com.phuonghai.inspection.core.network.NetworkMonitor
import com.phuonghai.inspection.core.storage.OfflineFileManager
import com.phuonghai.inspection.core.sync.SyncWorker
import com.phuonghai.inspection.data.local.dao.LocalReportDao
import com.phuonghai.inspection.data.local.entity.LocalReportEntity
import com.phuonghai.inspection.data.local.entity.toDomainModel
import com.phuonghai.inspection.data.local.entity.toLocalEntity
import com.phuonghai.inspection.domain.model.AssignStatus
import com.phuonghai.inspection.domain.model.Report
import com.phuonghai.inspection.domain.model.SyncStatus
import com.phuonghai.inspection.domain.repository.IReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class OfflineReportRepository @Inject constructor(
    private val localReportDao: LocalReportDao,
    private val onlineReportRepository: IReportRepository,
    private val networkMonitor: NetworkMonitor,
    private val fileManager: OfflineFileManager,
    @ApplicationContext private val context: Context
) : IReportRepository {

    companion object {
        private const val TAG = "OfflineReportRepository"
    }

    override suspend fun createReport(report: Report): Result<String> {
        return try {
            val isConnected = networkMonitor.isConnected.first()

            if (isConnected && report.assignStatus != AssignStatus.DRAFT) {
                // Online: Try to create report directly
                Log.d(TAG, "Creating report online: ${report.title}")

                // Ensure the remote repository always receives a synced report
                val syncedReport = report.copy(syncStatus = SyncStatus.SYNCED)
                val result = onlineReportRepository.createReport(syncedReport)

                if (result.isSuccess) {
                    val reportId = result.getOrNull()!!
                    val localReport = report.copy(
                        reportId = reportId,
                        createdAt = report.createdAt ?: Timestamp.now(),
                        syncStatus = SyncStatus.SYNCED
                    )
                    localReportDao.insertReport(localReport.toLocalEntity())
                    val unsyncedCount = localReportDao.getUnsyncedReportsCountForInspector(localReport.inspectorId)
                    localReportDao.trimReports(localReport.inspectorId, 30 + unsyncedCount)
                }

                result
            } else {
                // Offline or Draft: Save locally
                Log.d(TAG, "Saving report offline: ${report.title}")
                val reportId = if (report.reportId.isBlank()) UUID.randomUUID().toString() else report.reportId
                val reportWithId = report.copy(
                    reportId = reportId,
                    createdAt = report.createdAt ?: Timestamp.now(),
                    syncStatus = if (isConnected) SyncStatus.SYNCED else SyncStatus.UNSYNCED
                )

                // Save to local database
                val localEntity = reportWithId.toLocalEntity()
                localReportDao.insertReport(localEntity)
                val unsyncedCount = localReportDao.getUnsyncedReportsCountForInspector(reportWithId.inspectorId)
                localReportDao.trimReports(reportWithId.inspectorId, 30 + unsyncedCount)

                // Schedule sync if not connected and not a draft
                if (!isConnected && report.assignStatus != AssignStatus.DRAFT) {
                    SyncWorker.scheduleImmediateSync(context)
                }

                Result.success(reportId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating report", e)
            Result.failure(e)
        }
    }

    override suspend fun updateReport(report: Report): Result<Unit> {
        return try {
            val isConnected = networkMonitor.isConnected.first()

            if (isConnected && report.assignStatus != AssignStatus.DRAFT) {
                // Online: Try to update report directly
                Log.d(TAG, "Updating report online: ${report.reportId}")
                val result = onlineReportRepository.updateReport(report)

                if (result.isSuccess) {
                    // Update local copy as well
                    val localEntity = report.toLocalEntity().copy(needsSync = false)
                    localReportDao.updateReport(localEntity)
                }

                result
            } else {
                // Offline or Draft: Update locally
                Log.d(TAG, "Updating report offline: ${report.reportId}")
                val updatedReport = report.copy(
                    syncStatus = if (isConnected) SyncStatus.SYNCED else SyncStatus.UNSYNCED
                )

                val localEntity = updatedReport.toLocalEntity()
                localReportDao.updateReport(localEntity)

                // Schedule sync if not connected and not a draft
                if (!isConnected && report.assignStatus != AssignStatus.DRAFT) {
                    SyncWorker.scheduleImmediateSync(context)
                }

                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating report", e)
            Result.failure(e)
        }
    }

    override suspend fun getReport(reportId: String): Result<Report?> {
        return try {
            // First try to get from local database
            val localReport = localReportDao.getReportById(reportId)
            if (localReport != null) {
                Log.d(TAG, "Found report locally: $reportId")
                return Result.success(localReport.toDomainModel())
            }

            // If not found locally and connected, try online
            val isConnected = networkMonitor.isConnected.first()
            if (isConnected) {
                Log.d(TAG, "Fetching report online: $reportId")
                val result = onlineReportRepository.getReport(reportId)

                // Cache the result locally if successful
                result.getOrNull()?.let { report ->
                    val localEntity = report.toLocalEntity().copy(needsSync = false)
                    localReportDao.insertReport(localEntity)
                }

                result
            } else {
                Log.d(TAG, "Report not found locally and no connection: $reportId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting report", e)
            Result.failure(e)
        }
    }

    override suspend fun getDraftReportByTaskId(taskId: String): Result<Report?> {
        return try {
            Log.d(TAG, "Getting draft report for task: $taskId")
            val localDraft = localReportDao.getDraftReportByTaskId(taskId)

            if (localDraft != null) {
                Result.success(localDraft.toDomainModel())
            } else {
                // Try online if connected
                val isConnected = networkMonitor.isConnected.first()
                if (isConnected) {
                    onlineReportRepository.getDraftReportByTaskId(taskId)
                } else {
                    Result.success(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting draft report", e)
            Result.failure(e)
        }
    }

    override suspend fun getDraftReportsByInspectorId(inspectorId: String): Result<List<Report>> {
        return try {
            Log.d(TAG, "Getting draft reports for inspector: $inspectorId")
            val localDrafts = localReportDao.getDraftReportsByInspectorId(inspectorId)
            val domainReports = localDrafts.map { it.toDomainModel() }

            Result.success(domainReports)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting draft reports", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteDraftReport(reportId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting draft report: $reportId")

            // Delete from local database
            localReportDao.deleteReportById(reportId)

            // Try to delete online if connected
            val isConnected = networkMonitor.isConnected.first()
            if (isConnected) {
                // Note: Add online delete method to ReportRepositoryImpl if needed
                // onlineReportRepository.deleteDraftReport(reportId)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting draft report", e)
            Result.failure(e)
        }
    }

    override suspend fun uploadImage(imageUri: Uri): Result<String> {
        return try {
            val isConnected = networkMonitor.isConnected.first()

            if (isConnected) {
                // Online: Upload directly
                Log.d(TAG, "Uploading image online")
                onlineReportRepository.uploadImage(imageUri)
            } else {
                // Offline: Save locally and return local path
                Log.d(TAG, "Saving image offline for later upload")
                val reportId = UUID.randomUUID().toString() // Temporary ID
                fileManager.saveImageLocally(imageUri, reportId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling image upload", e)
            Result.failure(e)
        }
    }

    override suspend fun uploadVideo(videoUri: Uri): Result<String> {
        return try {
            val isConnected = networkMonitor.isConnected.first()

            if (isConnected) {
                // Online: Upload directly
                Log.d(TAG, "Uploading video online")
                onlineReportRepository.uploadVideo(videoUri)
            } else {
                // Offline: Save locally and return local path
                Log.d(TAG, "Saving video offline for later upload")
                val reportId = UUID.randomUUID().toString() // Temporary ID
                fileManager.saveVideoLocally(videoUri, reportId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling video upload", e)
            Result.failure(e)
        }
    }

    // Enhanced method for saving reports with media files offline
    suspend fun createReportWithMedia(
        report: Report,
        imageUris: List<Uri>,
        videoUri: Uri?
    ): Result<String> {
        return try {
            val reportId = if (report.reportId.isBlank()) UUID.randomUUID().toString() else report.reportId
            val isConnected = networkMonitor.isConnected.first()

            var localImagePath = ""
            var localVideoPath = ""
            var imageUrl = ""
            var videoUrl = ""

            if (isConnected && report.assignStatus != AssignStatus.DRAFT) {
                // Online: Upload media and create report
                Log.d(TAG, "Creating report with media online: $reportId")

                if (imageUris.isNotEmpty()) {
                    val uploadResult = onlineReportRepository.uploadImage(imageUris.first())
                    imageUrl = uploadResult.getOrNull() ?: ""
                }

                if (videoUri != null) {
                    val uploadResult = onlineReportRepository.uploadVideo(videoUri)
                    videoUrl = uploadResult.getOrNull() ?: ""
                }

                val reportWithMedia = report.copy(
                    reportId = reportId,
                    imageUrl = imageUrl,
                    videoUrl = videoUrl,
                    createdAt = report.createdAt ?: Timestamp.now()
                )

                return onlineReportRepository.createReport(reportWithMedia)
            } else {
                // Offline: Save media locally
                Log.d(TAG, "Saving report with media offline: $reportId")

                if (imageUris.isNotEmpty()) {
                    val saveResult = fileManager.saveImageLocally(imageUris.first(), reportId)
                    localImagePath = saveResult.getOrNull() ?: ""
                }

                if (videoUri != null) {
                    val saveResult = fileManager.saveVideoLocally(videoUri, reportId)
                    localVideoPath = saveResult.getOrNull() ?: ""
                }

                val reportWithMedia = report.copy(
                    reportId = reportId,
                    createdAt = report.createdAt ?: Timestamp.now(),
                    syncStatus = SyncStatus.UNSYNCED
                )

                val localEntity = reportWithMedia.toLocalEntity().copy(
                    localImagePath = localImagePath,
                    localVideoPath = localVideoPath,
                    needsSync = report.assignStatus != AssignStatus.DRAFT
                )

                localReportDao.insertReport(localEntity)
                val unsyncedCount = localReportDao.getUnsyncedReportsCountForInspector(reportWithMedia.inspectorId)
                localReportDao.trimReports(reportWithMedia.inspectorId, 30 + unsyncedCount)

                // Schedule sync if not a draft
                if (report.assignStatus != AssignStatus.DRAFT) {
                    SyncWorker.scheduleImmediateSync(context)
                }

                Result.success(reportId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating report with media", e)
            Result.failure(e)
        }
    }

    // Delegate methods to online repository for supervisor functions
    override suspend fun getReportsBySupervisorId(supervisorId: String): Result<List<Report>> {
        return onlineReportRepository.getReportsBySupervisorId(supervisorId)
    }

    override suspend fun updateStatus(reportId: String, status: String): Result<Unit> {
        return onlineReportRepository.updateStatus(reportId, status)
    }

    override fun getReportsByInspectorId(inspectorId: String): Flow<List<Report>> {
        return flow {
            val localReports = localReportDao.getReportsByInspectorId(inspectorId).first()

            if (networkMonitor.isConnected.first()) {
                val unsyncedIds = localReports.filter { it.needsSync }.map { it.reportId }.toSet()
                val remoteReports = onlineReportRepository.getReportsByInspectorId(inspectorId).first()

                remoteReports.forEach { report ->
                    val localReport = localReportDao.getReportById(report.reportId)
                    if (localReport?.needsSync == true) {
                        // Skip overwriting local report with unsynced changes
                        return@forEach
                    }

                    val entity = report.copy(syncStatus = SyncStatus.SYNCED)
                        .toLocalEntity()
                    localReportDao.insertReport(entity)
                }
                val unsyncedCount = localReportDao.getUnsyncedReportsCountForInspector(inspectorId)
                localReportDao.trimReports(inspectorId, 30 + unsyncedCount)
            }

            emitAll(
                localReportDao.getReportsByInspectorId(inspectorId)
                    .map { entities -> entities.map { it.toDomainModel() } }
            )
        }
    }
}