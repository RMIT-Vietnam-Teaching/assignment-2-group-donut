package com.phuonghai.inspection.domain.repository

import com.phuonghai.inspection.domain.model.Report

interface IReportRepository {
    suspend fun createReport(report: Report): Result<String>
    suspend fun updateReport(report: Report): Result<Unit>
    suspend fun getReport(reportId: String): Result<Report?>
}