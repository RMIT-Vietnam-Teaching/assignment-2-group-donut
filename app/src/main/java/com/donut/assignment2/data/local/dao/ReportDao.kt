package com.donut.assignment2.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.donut.assignment2.data.local.entities.ReportEntity
import java.time.LocalDateTime

@Dao
interface ReportDao {

    // 🔍 Basic CRUD Operations
    @Query("SELECT * FROM reports WHERE id = :id")
    suspend fun getReportById(id: String): ReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Update
    suspend fun updateReport(report: ReportEntity)

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteReport(id: String)

    @Delete
    suspend fun deleteReport(report: ReportEntity)

    // 📊 All Reports Queries
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReportsFlow(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    suspend fun getAllReports(): List<ReportEntity>

    // 👤 Inspector-specific Queries (using phone number)
    @Query("SELECT * FROM reports WHERE inspectorPhone = :inspectorPhone ORDER BY createdAt DESC")
    suspend fun getReportsByInspector(inspectorPhone: String): List<ReportEntity>

    @Query("SELECT * FROM reports WHERE inspectorPhone = :inspectorPhone ORDER BY createdAt DESC")
    fun getReportsByInspectorFlow(inspectorPhone: String): Flow<List<ReportEntity>>

    @Query("SELECT COUNT(*) FROM reports WHERE inspectorPhone = :inspectorPhone")
    suspend fun countReportsByInspector(inspectorPhone: String): Int

    @Query("SELECT COUNT(*) FROM reports WHERE inspectorPhone = :inspectorPhone AND status = :status")
    suspend fun countReportsByInspectorAndStatus(inspectorPhone: String, status: String): Int

    // 👨‍💼 Supervisor-specific Queries (using phone number)
    @Query("SELECT * FROM reports WHERE supervisorPhone = :supervisorPhone ORDER BY reviewedAt DESC")
    suspend fun getReportsBySupervisor(supervisorPhone: String): List<ReportEntity>

    @Query("SELECT * FROM reports WHERE supervisorPhone = :supervisorPhone ORDER BY reviewedAt DESC")
    fun getReportsBySupervisorFlow(supervisorPhone: String): Flow<List<ReportEntity>>

    @Query("SELECT COUNT(*) FROM reports WHERE supervisorPhone = :supervisorPhone")
    suspend fun countReportsBySupervisor(supervisorPhone: String): Int

    // 📋 Status-based Queries
    @Query("SELECT * FROM reports WHERE status = :status ORDER BY createdAt DESC")
    suspend fun getReportsByStatus(status: String): List<ReportEntity>

    @Query("SELECT * FROM reports WHERE status = :status ORDER BY createdAt DESC")
    fun getReportsByStatusFlow(status: String): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE status IN ('SUBMITTED', 'UNDER_REVIEW') ORDER BY submittedAt ASC")
    fun getPendingReportsFlow(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE status IN ('SUBMITTED', 'UNDER_REVIEW') ORDER BY submittedAt ASC")
    suspend fun getPendingReports(): List<ReportEntity>

    @Query("SELECT * FROM reports WHERE status = 'APPROVED' ORDER BY reviewedAt DESC")
    fun getApprovedReportsFlow(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE status = 'APPROVED' ORDER BY reviewedAt DESC")
    suspend fun getApprovedReports(): List<ReportEntity>

    @Query("SELECT * FROM reports WHERE status = 'REJECTED' ORDER BY reviewedAt DESC")
    fun getRejectedReportsFlow(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE status = 'DRAFT' ORDER BY updatedAt DESC")
    fun getDraftReportsFlow(): Flow<List<ReportEntity>>

    // 🔄 Status Update Operations
    @Query("UPDATE reports SET status = :status, supervisorPhone = :supervisorPhone, reviewedAt = :reviewedAt WHERE id = :reportId")
    suspend fun updateReportStatus(
        reportId: String,
        status: String,
        supervisorPhone: String?,
        reviewedAt: LocalDateTime?
    )

    @Query("UPDATE reports SET status = :status WHERE id = :reportId")
    suspend fun updateReportStatus(reportId: String, status: String)

    @Query("UPDATE reports SET supervisorNotes = :notes, supervisorPhone = :supervisorPhone WHERE id = :reportId")
    suspend fun updateSupervisorNotes(reportId: String, notes: String, supervisorPhone: String)

    // 📅 Date Range Queries
    @Query("SELECT * FROM reports WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    suspend fun getReportsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<ReportEntity>

    @Query("SELECT * FROM reports WHERE inspectorPhone = :inspectorPhone AND createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    suspend fun getReportsByInspectorAndDateRange(
        inspectorPhone: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<ReportEntity>

    @Query("SELECT * FROM reports WHERE submittedAt BETWEEN :startDate AND :endDate ORDER BY submittedAt DESC")
    suspend fun getReportsBySubmissionDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<ReportEntity>

    // 🔍 Search Queries
    @Query("SELECT * FROM reports WHERE title LIKE :searchTerm OR description LIKE :searchTerm OR location LIKE :searchTerm ORDER BY createdAt DESC")
    suspend fun searchReports(searchTerm: String): List<ReportEntity>

    @Query("SELECT * FROM reports WHERE inspectorPhone = :inspectorPhone AND (title LIKE :searchTerm OR description LIKE :searchTerm OR location LIKE :searchTerm) ORDER BY createdAt DESC")
    suspend fun searchReportsByInspector(inspectorPhone: String, searchTerm: String): List<ReportEntity>

    // 📈 Statistics Queries
    @Query("SELECT status, COUNT(*) as count FROM reports GROUP BY status")
    suspend fun getReportStatusCounts(): List<ReportStatusCount>

    @Query("SELECT status, COUNT(*) as count FROM reports WHERE inspectorPhone = :inspectorPhone GROUP BY status")
    suspend fun getReportStatusCountsByInspector(inspectorPhone: String): List<ReportStatusCount>

    @Query("SELECT inspectorPhone, COUNT(*) as count FROM reports GROUP BY inspectorPhone ORDER BY count DESC")
    suspend fun getReportCountsByInspector(): List<InspectorReportCount>

    @Query("SELECT COUNT(*) FROM reports WHERE createdAt >= :date")
    suspend fun getReportsCreatedSince(date: LocalDateTime): Int

    @Query("SELECT COUNT(*) FROM reports WHERE submittedAt >= :date")
    suspend fun getReportsSubmittedSince(date: LocalDateTime): Int

    // 🗑️ Cleanup Operations
    @Query("DELETE FROM reports WHERE status = 'DRAFT' AND updatedAt < :cutoffDate")
    suspend fun deleteOldDraftReports(cutoffDate: LocalDateTime): Int

    @Query("DELETE FROM reports WHERE createdAt < :cutoffDate")
    suspend fun deleteOldReports(cutoffDate: LocalDateTime): Int

    @Query("DELETE FROM reports")
    suspend fun deleteAllReports()

    // 🔄 Sync Operations
    @Query("SELECT * FROM reports WHERE updatedAt > :lastSyncTime ORDER BY updatedAt ASC")
    suspend fun getReportsModifiedSince(lastSyncTime: LocalDateTime): List<ReportEntity>

    @Query("UPDATE reports SET updatedAt = :updatedAt WHERE id = :reportId")
    suspend fun updateReportTimestamp(reportId: String, updatedAt: LocalDateTime)

    // 📱 Inspector Dashboard Specific Queries
    @Query("SELECT * FROM reports WHERE inspectorPhone = :inspectorPhone ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun getRecentReportsByInspector(inspectorPhone: String, limit: Int = 5): List<ReportEntity>

    @Query("SELECT COUNT(*) FROM reports WHERE inspectorPhone = :inspectorPhone AND status = 'DRAFT'")
    suspend fun countDraftReportsByInspector(inspectorPhone: String): Int

    @Query("SELECT COUNT(*) FROM reports WHERE inspectorPhone = :inspectorPhone AND status IN ('SUBMITTED', 'UNDER_REVIEW')")
    suspend fun countPendingReportsByInspector(inspectorPhone: String): Int

    @Query("SELECT COUNT(*) FROM reports WHERE inspectorPhone = :inspectorPhone AND status = 'APPROVED'")
    suspend fun countApprovedReportsByInspector(inspectorPhone: String): Int

    @Query("SELECT COUNT(*) FROM reports WHERE inspectorPhone = :inspectorPhone AND status = 'REJECTED'")
    suspend fun countRejectedReportsByInspector(inspectorPhone: String): Int

    // 👨‍💼 Supervisor Dashboard Specific Queries
    @Query("SELECT * FROM reports WHERE supervisorPhone = :supervisorPhone AND status IN ('SUBMITTED', 'UNDER_REVIEW') ORDER BY submittedAt ASC LIMIT :limit")
    suspend fun getPendingReportsBySupervisor(supervisorPhone: String, limit: Int = 10): List<ReportEntity>

    @Query("SELECT COUNT(*) FROM reports WHERE supervisorPhone = :supervisorPhone AND status IN ('SUBMITTED', 'UNDER_REVIEW')")
    suspend fun countPendingReportsBySupervisor(supervisorPhone: String): Int
}

// 📊 Data classes for statistical queries
data class ReportStatusCount(
    val status: String,
    val count: Int
)

data class InspectorReportCount(
    val inspectorPhone: String,
    val count: Int
)