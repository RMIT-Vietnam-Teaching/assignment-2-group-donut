package com.donut.assignment2.domain.repository

import com.donut.assignment2.domain.model.*

interface PhotoRepository {
    suspend fun savePhoto(photo: Photo, imageData: ByteArray): Result<String>
    suspend fun getPhotoById(photoId: String): Result<Photo?>
    suspend fun getPhotosByReportId(reportId: String): Result<List<Photo>>
    suspend fun deletePhoto(photoId: String): Result<Unit>
    suspend fun updatePhotoMLStatus(photoId: String, status: MLProcessingStatus): Result<Unit>
    suspend fun getPhotosForMLProcessing(): Result<List<Photo>>
}