package com.phuonghai.inspection.domain.repository

import android.net.Uri
import com.phuonghai.inspection.domain.model.Report

interface IReportRepository {
    suspend fun createReport(report: Report): Result<String>
    suspend fun updateReport(report: Report): Result<Unit>
    suspend fun getReport(reportId: String): Result<Report?>
    suspend fun uploadImage(imageUri: Uri): Result<String>
    suspend fun uploadVideo(videoUri: Uri): Result<String>
}