package com.phuonghai.inspection.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import com.phuonghai.inspection.domain.model.Report
import com.phuonghai.inspection.domain.repository.IReportRepository
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : IReportRepository {

    companion object {
        private const val TAG = "ReportRepository"
        private const val REPORTS_COLLECTION = "reports"
    }

    override suspend fun createReport(report: Report): Result<String> {
        return try {
            val reportId = UUID.randomUUID().toString()
            val reportWithId = report.copy(
                reportId = reportId,
                createdAt = Timestamp.now()
            )

            firestore.collection(REPORTS_COLLECTION)
                .document(reportId)
                .set(reportWithId)
                .await()

            Log.d(TAG, "Report created successfully: $reportId")
            Result.success(reportId)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating report", e)
            Result.failure(e)
        }
    }

    override suspend fun updateReport(report: Report): Result<Unit> {
        return try {
            firestore.collection(REPORTS_COLLECTION)
                .document(report.reportId)
                .set(report)
                .await()

            Log.d(TAG, "Report updated successfully: ${report.reportId}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating report", e)
            Result.failure(e)
        }
    }

    override suspend fun getReport(reportId: String): Result<Report?> {
        return try {
            val document = firestore.collection(REPORTS_COLLECTION)
                .document(reportId)
                .get()
                .await()

            val report = if (document.exists()) {
                document.toObject(Report::class.java)
            } else {
                null
            }

            Log.d(TAG, "Retrieved report: $reportId")
            Result.success(report)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting report", e)
            Result.failure(e)
        }
    }

    private val storage = Firebase.storage

    override suspend fun uploadImage(imageUri: Uri): Result<String> {
        return try {
            val imageRef = storage.reference.child("images/${UUID.randomUUID()}")
            val uploadTask = imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image", e)
            Result.failure(e)
        }
    }

    override suspend fun uploadVideo(videoUri: Uri): Result<String> {
        return try {
            val videoRef = storage.reference.child("videos/${UUID.randomUUID()}")
            val uploadTask = videoRef.putFile(videoUri).await()
            val downloadUrl = videoRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading video", e)
            Result.failure(e)
        }
    }

}