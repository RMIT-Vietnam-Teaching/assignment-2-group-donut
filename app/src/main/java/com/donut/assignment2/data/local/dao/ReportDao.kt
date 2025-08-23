package com.donut.assignment2.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.donut.assignment2.data.local.entities.ReportEntity

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReportsFlow(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE id = :id")
    suspend fun getReportById(id: String): ReportEntity?

    @Query("SELECT * FROM reports WHERE inspectorId = :inspectorId ORDER BY createdAt DESC")
    suspend fun getReportsByInspector(inspectorId: String): List<ReportEntity>

    @Query("SELECT * FROM reports WHERE inspectorId = :inspectorId ORDER BY createdAt DESC")
    fun getReportsByInspectorFlow(inspectorId: String): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE status IN ('SUBMITTED', 'UNDER_REVIEW') ORDER BY submittedAt ASC")
    fun getPendingReportsFlow(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE status = 'APPROVED' ORDER BY reviewedAt DESC")
    fun getApprovedReportsFlow(): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Update
    suspend fun updateReport(report: ReportEntity)

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteReport(id: String)

    @Query("UPDATE reports SET status = :status WHERE id = :reportId")
    suspend fun updateReportStatus(reportId: String, status: String)
}