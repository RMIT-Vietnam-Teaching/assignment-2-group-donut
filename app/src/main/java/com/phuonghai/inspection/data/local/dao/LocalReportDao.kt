package com.phuonghai.inspection.data.local.dao

import androidx.room.*
import com.phuonghai.inspection.data.local.entity.LocalReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalReportDao {

    @Query("SELECT * FROM local_reports WHERE inspectorId = :inspectorId")
    fun getReportsByInspectorId(inspectorId: String): Flow<List<LocalReportEntity>>

    @Query("SELECT * FROM local_reports WHERE reportId = :reportId")
    suspend fun getReportById(reportId: String): LocalReportEntity?

    @Query("SELECT * FROM local_reports WHERE taskId = :taskId AND assignStatus = 'DRAFT' ORDER BY createdAt DESC LIMIT 1")
    suspend fun getDraftReportByTaskId(taskId: String): LocalReportEntity?

    @Query("SELECT * FROM local_reports WHERE inspectorId = :inspectorId AND assignStatus = 'DRAFT'")
    suspend fun getDraftReportsByInspectorId(inspectorId: String): List<LocalReportEntity>

    @Query("SELECT * FROM local_reports WHERE needsSync = 1 ORDER BY createdAt ASC")
    suspend fun getUnsyncedReports(): List<LocalReportEntity>

    @Query("SELECT COUNT(*) FROM local_reports WHERE needsSync = 1")
    suspend fun getUnsyncedReportsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: LocalReportEntity)

    @Update
    suspend fun updateReport(report: LocalReportEntity)

    @Delete
    suspend fun deleteReport(report: LocalReportEntity)

    @Query("DELETE FROM local_reports WHERE reportId = :reportId")
    suspend fun deleteReportById(reportId: String)

    @Query("UPDATE local_reports SET needsSync = 0, syncStatus = 'SYNCED' WHERE reportId = :reportId")
    suspend fun markAsSynced(reportId: String)

    @Query("UPDATE local_reports SET lastSyncAttempt = :timestamp, syncRetryCount = syncRetryCount + 1 WHERE reportId = :reportId")
    suspend fun updateSyncAttempt(reportId: String, timestamp: Long)

    @Query("UPDATE local_reports SET syncRetryCount = 0 WHERE reportId = :reportId")
    suspend fun resetSyncRetryCount(reportId: String)

    @Query("SELECT * FROM local_reports WHERE needsSync = 1 AND syncRetryCount < 3")
    suspend fun getReportsForSync(): List<LocalReportEntity>

    @Query("DELETE FROM local_reports WHERE createdAt < :cutoffTime AND needsSync = 0")
    suspend fun deleteOldSyncedReports(cutoffTime: Long)
}