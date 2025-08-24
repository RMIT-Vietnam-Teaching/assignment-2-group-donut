package com.donut.assignment2.data.repository

import android.content.Context
import com.donut.assignment2.data.local.dao.*
import com.donut.assignment2.data.mapper.*
import com.donut.assignment2.domain.model.*
import com.donut.assignment2.domain.repository.*
import com.donut.assignment2.data.local.entities.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepositoryImpl @Inject constructor(
    private val photoDao: PhotoDao,
    private val photoMapper: PhotoMapper,
    @ApplicationContext private val context: Context
) : PhotoRepository {

    override suspend fun savePhoto(photo: Photo, imageData: ByteArray): Result<String> {
        return try {
            // Save image file to internal storage
            val fileName = "${photo.id}_${photo.fileName}"
            val file = File(context.filesDir, "photos")
            if (!file.exists()) {
                file.mkdirs()
            }

            val imageFile = File(file, fileName)
            imageFile.writeBytes(imageData)

            // Save photo entity with file path
            val photoEntity = photoMapper.toEntity(photo.copy(filePath = imageFile.absolutePath))
            photoDao.insertPhoto(photoEntity)

            Result.success(photo.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPhotoById(photoId: String): Result<Photo?> {
        return try {
            val photoEntity = photoDao.getPhotoById(photoId)
            val photo = photoEntity?.let { photoMapper.fromEntity(it) }
            Result.success(photo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPhotosByReportId(reportId: String): Result<List<Photo>> {
        return try {
            val photoEntities = photoDao.getPhotosByReportId(reportId)
            val photos = photoEntities.map { photoMapper.fromEntity(it) }
            Result.success(photos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePhoto(photoId: String): Result<Unit> {
        return try {
            val photoEntity = photoDao.getPhotoById(photoId)
            if (photoEntity != null) {
                // Delete file from storage
                val file = File(photoEntity.filePath)
                if (file.exists()) {
                    file.delete()
                }

                // Delete from database
                photoDao.deletePhoto(photoEntity)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePhotoMLStatus(photoId: String, status: MLProcessingStatus): Result<Unit> {
        return try {
            val photoEntity = photoDao.getPhotoById(photoId)
            if (photoEntity != null) {
                val updatedEntity = photoEntity.copy(
                    isProcessedByML = status == MLProcessingStatus.COMPLETED
                )
                photoDao.updatePhoto(updatedEntity)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPhotosForMLProcessing(): Result<List<Photo>> {
        return try {
            // This would need a proper query - simplified for now
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
