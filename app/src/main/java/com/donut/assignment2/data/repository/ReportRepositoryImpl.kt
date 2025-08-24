package com.donut.assignment2.data.repository

import android.util.Log
import com.donut.assignment2.data.local.dao.*
import com.donut.assignment2.data.mapper.*
import com.donut.assignment2.domain.model.*
import com.donut.assignment2.domain.repository.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val reportDao: ReportDao,
    private val photoDao: PhotoDao,
    private val defectDao: DefectDao,
    private val reportMapper: ReportMapper,
    private val photoMapper: PhotoMapper,
    private val defectMapper: DefectMapper,
    private val firestore: FirebaseFirestore
) : ReportRepository {

    companion object {
        private const val TAG = "ReportRepositoryImpl"
        private const val REPORTS_COLLECTION = "reports"
    }

    // 🔍 Basic CRUD Operations
    override suspend fun createReport(report: Report): Result<String> {
        return try {
            Log.d(TAG, "Creating report: ${report.id}")

            // Save to local database
            val reportEntity = reportMapper.toEntity(report)
            reportDao.insertReport(reportEntity)

            // Save to Firestore
            val firestoreData = reportMapper.toFirestoreMap(report)
            firestore.collection(REPORTS_COLLECTION)
                .document(report.id)
                .set(firestoreData)
                .await()

            Log.d(TAG, "Report created successfully: ${report.id}")
            Result.success(report.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating report: ${report.id}", e)
            Result.failure(Exception("Không thể tạo báo cáo: ${e.message}"))
        }
    }

    override suspend fun updateReport(report: Report): Result<Unit> {
        return try {
            Log.d(TAG, "Updating report: ${report.id}")

            // Update in local database
            val reportEntity = reportMapper.toEntity(report)
            reportDao.updateReport(reportEntity)

            // Update in Firestore
            val firestoreData = reportMapper.toFirestoreMap(report)
            firestore.collection(REPORTS_COLLECTION)
                .document(report.id)
                .set(firestoreData) // Use set to overwrite completely
                .await()

            Log.d(TAG, "Report updated successfully: ${report.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating report: ${report.id}", e)
            Result.failure(Exception("Không thể cập nhật báo cáo: ${e.message}"))
        }
    }

    override suspend fun getReportById(id: String): Result<Report?> {
        return try {
            Log.d(TAG, "Getting report by ID: $id")

            // Try local cache first
            val reportEntity = reportDao.getReportById(id)
            if (reportEntity != null) {
                // Load related photos and defects
                val photos = photoDao.getPhotosByReportId(id).map { photoMapper.fromEntity(it) }
                val defects = defectDao.getDefectsByReportId(id).map { defectMapper.fromEntity(it) }

                val report = reportMapper.fromEntity(reportEntity).copy(
                    photos = photos,
                    defects = defects
                )

                Log.d(TAG, "Report found in local cache: $id")
                return Result.success(report)
            }

            // Fallback to Firestore
            val document = firestore.collection(REPORTS_COLLECTION)
                .document(id)
                .get()
                .await()

            if (document.exists() && document.data != null) {
                val report = reportMapper.fromFirestoreMap(document.data!!, document.id)

                // Cache locally
                try {
                    reportDao.insertReport(reportMapper.toEntity(report))
                } catch (cacheException: Exception) {
                    Log.w(TAG, "Failed to cache report", cacheException)
                }

                Log.d(TAG, "Report found in Firestore: $id")
                Result.success(report)
            } else {
                Log.w(TAG, "Report not found: $id")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting report: $id", e)
            Result.failure(Exception("Không thể lấy báo cáo: ${e.message}"))
        }
    }

    override suspend fun deleteReport(id: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting report: $id")

            // Delete from local database (cascade will handle photos/defects)
            reportDao.deleteReport(id)

            // Delete from Firestore
            firestore.collection(REPORTS_COLLECTION)
                .document(id)
                .delete()
                .await()

            Log.d(TAG, "Report deleted successfully: $id")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting report: $id", e)
            Result.failure(Exception("Không thể xóa báo cáo: ${e.message}"))
        }
    }

    // 👤 Inspector Functions
    override suspend fun getReportsByInspector(inspectorPhone: String): Result<List<Report>> {
        return try {
            Log.d(TAG, "Getting reports for inspector: $inspectorPhone")

            // Try local cache first
            val localReports = reportDao.getReportsByInspector(inspectorPhone)
                .map { entity ->
                    val photos = photoDao.getPhotosByReportId(entity.id).map { photoMapper.fromEntity(it) }
                    val defects = defectDao.getDefectsByReportId(entity.id).map { defectMapper.fromEntity(it) }

                    reportMapper.fromEntity(entity).copy(
                        photos = photos,
                        defects = defects
                    )
                }

            try {
                // Get fresh data from Firestore
                val querySnapshot = firestore.collection(REPORTS_COLLECTION)
                    .whereEqualTo("inspectorPhone", inspectorPhone)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val firestoreReports = querySnapshot.documents.mapNotNull { document ->
                    try {
                        document.data?.let { data ->
                            reportMapper.fromFirestoreMap(data, document.id)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to map report: ${document.id}", e)
                        null
                    }
                }

                // Cache fresh data
                try {
                    firestoreReports.forEach { report ->
                        reportDao.insertReport(reportMapper.toEntity(report))
                    }
                    Log.d(TAG, "Cached ${firestoreReports.size} reports for inspector: $inspectorPhone")
                } catch (cacheException: Exception) {
                    Log.w(TAG, "Failed to cache reports", cacheException)
                }

                Log.d(TAG, "Found ${firestoreReports.size} reports for inspector: $inspectorPhone")
                Result.success(firestoreReports)

            } catch (firestoreException: Exception) {
                Log.w(TAG, "Firestore failed, returning cached reports", firestoreException)
                Result.success(localReports)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting reports for inspector: $inspectorPhone", e)
            Result.failure(Exception("Không thể lấy danh sách báo cáo: ${e.message}"))
        }
    }

    override fun getReportsByInspectorFlow(inspectorPhone: String): Flow<List<Report>> {
        return reportDao.getReportsByInspectorFlow(inspectorPhone)
            .map { entities ->
                entities.map { entity ->
                    val photos = photoDao.getPhotosByReportId(entity.id).map { photoMapper.fromEntity(it) }
                    val defects = defectDao.getDefectsByReportId(entity.id).map { defectMapper.fromEntity(it) }

                    reportMapper.fromEntity(entity).copy(
                        photos = photos,
                        defects = defects
                    )
                }
            }
            .catch { e ->
                Log.e(TAG, "Error in reports flow for inspector: $inspectorPhone", e)
                emit(emptyList())
            }
    }

    // 👨‍💼 Supervisor Functions
    override suspend fun getAllReports(): Result<List<Report>> {
        return try {
            Log.d(TAG, "Getting all reports")

            val reports = reportDao.getAllReports().map { entity ->
                val photos = photoDao.getPhotosByReportId(entity.id).map { photoMapper.fromEntity(it) }
                val defects = defectDao.getDefectsByReportId(entity.id).map { defectMapper.fromEntity(it) }

                reportMapper.fromEntity(entity).copy(
                    photos = photos,
                    defects = defects
                )
            }

            Log.d(TAG, "Found ${reports.size} total reports")
            Result.success(reports)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all reports", e)
            Result.failure(Exception("Không thể lấy danh sách báo cáo: ${e.message}"))
        }
    }

    override fun getPendingReportsFlow(): Flow<List<Report>> {
        return reportDao.getPendingReportsFlow()
            .map { entities -> reportMapper.fromEntities(entities) }
            .catch { e ->
                Log.e(TAG, "Error in pending reports flow", e)
                emit(emptyList())
            }
    }

    override fun getApprovedReportsFlow(): Flow<List<Report>> {
        return reportDao.getApprovedReportsFlow()
            .map { entities -> reportMapper.fromEntities(entities) }
            .catch { e ->
                Log.e(TAG, "Error in approved reports flow", e)
                emit(emptyList())
            }
    }

    override suspend fun updateReportStatus(reportId: String, status: ReportStatus): Result<Unit> {
        return try {
            Log.d(TAG, "Updating report status: $reportId -> $status")

            val now = LocalDateTime.now()

            // Update local database
            reportDao.updateReportStatus(reportId, status.name)

            // Update Firestore
            val updates = hashMapOf<String, Any>(
                "status" to status.name,
                "updatedAt" to now.toString()
            )

            // Add review timestamp if approved/rejected
            if (status == ReportStatus.APPROVED || status == ReportStatus.REJECTED) {
                updates["reviewedAt"] = now.toString()
            }

            firestore.collection(REPORTS_COLLECTION)
                .document(reportId)
                .update(updates)
                .await()

            Log.d(TAG, "Report status updated successfully: $reportId -> $status")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating report status: $reportId", e)
            Result.failure(Exception("Không thể cập nhật trạng thái báo cáo: ${e.message}"))
        }
    }

    override suspend fun assignSupervisorToReport(reportId: String, supervisorPhone: String, notes: String): Result<Unit> {
        return try {
            Log.d(TAG, "Assigning supervisor to report: $reportId -> $supervisorPhone")

            val now = LocalDateTime.now()

            // Update local database
            reportDao.updateSupervisorNotes(reportId, notes, supervisorPhone)

            // Update Firestore
            val updates = hashMapOf<String, Any>(
                "supervisorPhone" to supervisorPhone,
                "supervisorNotes" to notes,
                "updatedAt" to now.toString()
            )

            firestore.collection(REPORTS_COLLECTION)
                .document(reportId)
                .update(updates)
                .await()

            Log.d(TAG, "Supervisor assigned successfully: $reportId -> $supervisorPhone")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error assigning supervisor to report: $reportId", e)
            Result.failure(Exception("Không thể phân công giám sát: ${e.message}"))
        }
    }

    // 📊 Statistics and Analytics
    override suspend fun getReportStatistics(): Result<ReportStatistics> {
        return try {
            Log.d(TAG, "Getting report statistics")

            val statusCounts = reportDao.getReportStatusCounts()
            val totalReports = statusCounts.sumOf { it.count }

            val statistics = ReportStatistics(
                totalReports = totalReports,
                draftReports = statusCounts.find { it.status == "DRAFT" }?.count ?: 0,
                submittedReports = statusCounts.find { it.status == "SUBMITTED" }?.count ?: 0,
                underReviewReports = statusCounts.find { it.status == "UNDER_REVIEW" }?.count ?: 0,
                approvedReports = statusCounts.find { it.status == "APPROVED" }?.count ?: 0,
                rejectedReports = statusCounts.find { it.status == "REJECTED" }?.count ?: 0
            )

            Log.d(TAG, "Report statistics calculated: $statistics")
            Result.success(statistics)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting report statistics", e)
            Result.failure(Exception("Không thể lấy thống kê báo cáo: ${e.message}"))
        }
    }

    override suspend fun getInspectorStatistics(inspectorPhone: String): Result<InspectorStatistics> {
        return try {
            Log.d(TAG, "Getting statistics for inspector: $inspectorPhone")

            val statusCounts = reportDao.getReportStatusCountsByInspector(inspectorPhone)
            val totalReports = statusCounts.sumOf { it.count }

            val statistics = InspectorStatistics(
                inspectorPhone = inspectorPhone,
                totalReports = totalReports,
                draftReports = statusCounts.find { it.status == "DRAFT" }?.count ?: 0,
                submittedReports = statusCounts.find { it.status == "SUBMITTED" }?.count ?: 0,
                approvedReports = statusCounts.find { it.status == "APPROVED" }?.count ?: 0,
                rejectedReports = statusCounts.find { it.status == "REJECTED" }?.count ?: 0,
                recentReports = reportDao.getRecentReportsByInspector(inspectorPhone, 5)
                    .map { reportMapper.fromEntity(it) }
            )

            Log.d(TAG, "Inspector statistics calculated: $statistics")
            Result.success(statistics)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting inspector statistics: $inspectorPhone", e)
            Result.failure(Exception("Không thể lấy thống kê inspector: ${e.message}"))
        }
    }

    // 🔍 Search and Filter
    override suspend fun searchReports(query: String): Result<List<Report>> {
        return try {
            Log.d(TAG, "Searching reports with query: $query")

            val searchTerm = "%$query%"
            val reports = reportDao.searchReports(searchTerm).map { entity ->
                val photos = photoDao.getPhotosByReportId(entity.id).map { photoMapper.fromEntity(it) }
                val defects = defectDao.getDefectsByReportId(entity.id).map { defectMapper.fromEntity(it) }

                reportMapper.fromEntity(entity).copy(
                    photos = photos,
                    defects = defects
                )
            }

            Log.d(TAG, "Found ${reports.size} reports matching query: $query")
            Result.success(reports)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching reports: $query", e)
            Result.failure(Exception("Không thể tìm kiếm báo cáo: ${e.message}"))
        }
    }

    override suspend fun getReportsByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<List<Report>> {
        return try {
            Log.d(TAG, "Getting reports by date range: $startDate to $endDate")

            val reports = reportDao.getReportsByDateRange(startDate, endDate)
                .map { reportMapper.fromEntity(it) }

            Log.d(TAG, "Found ${reports.size} reports in date range")
            Result.success(reports)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting reports by date range", e)
            Result.failure(Exception("Không thể lấy báo cáo theo thời gian: ${e.message}"))
        }
    }

    // 🔄 Sync Operations
    override suspend fun syncReports(): Result<Unit> {
        return try {
            Log.d(TAG, "Starting reports sync")

            // Get all local reports that need syncing
            val lastSyncTime = LocalDateTime.now().minusDays(1) // Simplified - should use actual last sync time
            val localReports = reportDao.getReportsModifiedSince(lastSyncTime)

            // Upload local changes to Firestore
            localReports.forEach { entity ->
                val report = reportMapper.fromEntity(entity)
                val firestoreData = reportMapper.toFirestoreMap(report)

                firestore.collection(REPORTS_COLLECTION)
                    .document(report.id)
                    .set(firestoreData)
                    .await()
            }

            Log.d(TAG, "Reports sync completed: ${localReports.size} reports synced")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing reports", e)
            Result.failure(Exception("Không thể đồng bộ báo cáo: ${e.message}"))
        }
    }

    override suspend fun clearLocalCache(): Result<Unit> {
        return try {
            reportDao.deleteAllReports()
            Log.d(TAG, "Local reports cache cleared")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing local cache", e)
            Result.failure(Exception("Không thể xóa cache: ${e.message}"))
        }
    }
}

// 📊 Data classes for statistics
data class ReportStatistics(
    val totalReports: Int,
    val draftReports: Int,
    val submittedReports: Int,
    val underReviewReports: Int,
    val approvedReports: Int,
    val rejectedReports: Int
) {
    val completionRate: Float
        get() = if (totalReports > 0) approvedReports.toFloat() / totalReports else 0f

    val pendingReports: Int
        get() = submittedReports + underReviewReports
}

data class InspectorStatistics(
    val inspectorPhone: String,
    val totalReports: Int,
    val draftReports: Int,
    val submittedReports: Int,
    val approvedReports: Int,
    val rejectedReports: Int,
    val recentReports: List<Report>
) {
    val completionRate: Float
        get() = if (totalReports > 0) approvedReports.toFloat() / totalReports else 0f

    val pendingWork: Int
        get() = draftReports + rejectedReports
}