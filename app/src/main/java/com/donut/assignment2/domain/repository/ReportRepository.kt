package com.donut.assignment2.domain.repository

import com.donut.assignment2.domain.model.Report
import com.donut.assignment2.domain.model.ReportStatus
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    suspend fun createReport(report: Report): Result<String>
    suspend fun updateReport(report: Report): Result<Unit>
    suspend fun getReportById(id: String): Result<Report?>
    suspend fun deleteReport(id: String): Result<Unit>

    // Inspector functions
    suspend fun getReportsByInspector(inspectorId: String): Result<List<Report>>
    fun getReportsByInspectorFlow(inspectorId: String): Flow<List<Report>>

    // Supervisor functions
    suspend fun getAllReports(): Result<List<Report>>
    fun getPendingReportsFlow(): Flow<List<Report>>
    fun getApprovedReportsFlow(): Flow<List<Report>>
    suspend fun updateReportStatus(reportId: String, status: ReportStatus): Result<Unit>
}