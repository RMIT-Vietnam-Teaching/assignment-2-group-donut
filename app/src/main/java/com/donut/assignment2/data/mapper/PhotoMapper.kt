package com.donut.assignment2.data.mapper

import com.donut.assignment2.data.local.entities.PhotoEntity
import com.donut.assignment2.domain.model.Photo
import com.donut.assignment2.data.local.entities.DefectEntity
import com.donut.assignment2.domain.model.Defect
import com.donut.assignment2.domain.model.DefectType
import com.donut.assignment2.domain.model.DefectSeverity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoMapper @Inject constructor() {

    fun toEntity(photo: Photo): PhotoEntity {
        return PhotoEntity(
            id = photo.id,
            reportId = photo.reportId,
            filePath = photo.filePath,
            fileName = photo.fileName,
            timestamp = photo.timestamp,
            latitude = photo.latitude,
            longitude = photo.longitude,
            isProcessedByML = photo.isProcessedByML
        )
    }

    fun fromEntity(entity: PhotoEntity): Photo {
        return Photo(
            id = entity.id,
            reportId = entity.reportId,
            filePath = entity.filePath,
            fileName = entity.fileName,
            timestamp = entity.timestamp,
            latitude = entity.latitude,
            longitude = entity.longitude,
            isProcessedByML = entity.isProcessedByML
        )
    }
}