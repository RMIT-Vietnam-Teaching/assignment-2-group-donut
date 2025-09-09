package com.phuonghai.inspection.domain.repository

import android.net.Uri
import com.phuonghai.inspection.domain.model.Report
import kotlinx.coroutines.flow.Flow

interface IReportRepository {
    suspend fun createReport(report: Report): Result<String>
    suspend fun updateReport(report: Report): Result<Unit>
    suspend fun getReport(reportId: String): Result<Report?>
    suspend fun uploadImage(imageUri: Uri): Result<String>
    suspend fun uploadVideo(videoUri: Uri): Result<String>
    suspend fun getDraftReportByTaskId(taskId: String): Result<Report?>
    suspend fun getDraftReportsByInspectorId(inspectorId: String): Result<List<Report>>
    suspend fun deleteDraftReport(reportId: String): Result<Unit>
    suspend fun getReportsBySupervisorId(supervisorId: String): Result<List<Report>>
    suspend fun updateStatus(reportId: String, status: String): Result<Unit>
    fun getReportsByInspectorId(inspectorId: String): Flow<List<Report>>
}