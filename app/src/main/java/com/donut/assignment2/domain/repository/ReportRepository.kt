package com.donut.assignment2.domain.repository

import com.donut.assignment2.data.repository.InspectorStatistics
import com.donut.assignment2.data.repository.ReportStatistics
import com.donut.assignment2.domain.model.Report
import com.donut.assignment2.domain.model.ReportStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface ReportRepository {
    // Basic CRUD
    suspend fun createReport(report: Report): Result<String>
    suspend fun updateReport(report: Report): Result<Unit>
    suspend fun getReportById(id: String): Result<Report?>
    suspend fun deleteReport(id: String): Result<Unit>

    // Inspector functions (using phone)
    suspend fun getReportsByInspector(inspectorPhone: String): Result<List<Report>>
    fun getReportsByInspectorFlow(inspectorPhone: String): Flow<List<Report>>

    // Supervisor functions
    suspend fun getAllReports(): Result<List<Report>>
    fun getPendingReportsFlow(): Flow<List<Report>>
    fun getApprovedReportsFlow(): Flow<List<Report>>
    suspend fun updateReportStatus(reportId: String, status: ReportStatus): Result<Unit>
    suspend fun assignSupervisorToReport(reportId: String, supervisorPhone: String, notes: String): Result<Unit>

    // Statistics
    suspend fun getReportStatistics(): Result<ReportStatistics>
    suspend fun getInspectorStatistics(inspectorPhone: String): Result<InspectorStatistics>

    // Search & Filter
    suspend fun searchReports(query: String): Result<List<Report>>
    suspend fun getReportsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Result<List<Report>>

    // Sync
    suspend fun syncReports(): Result<Unit>
    suspend fun clearLocalCache(): Result<Unit>
}