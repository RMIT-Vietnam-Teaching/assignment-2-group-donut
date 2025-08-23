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
class DefectMapper @Inject constructor() {

    fun toEntity(defect: Defect): DefectEntity {
        return DefectEntity(
            id = defect.id,
            reportId = defect.reportId,
            photoId = defect.photoId,
            type = defect.type.name,
            severity = defect.severity.name,
            description = defect.description,
            confidence = defect.confidence,
            isMLDetected = defect.isMLDetected,
            isVerified = defect.isVerified,
            createdAt = defect.createdAt
        )
    }

    fun fromEntity(entity: DefectEntity): Defect {
        return Defect(
            id = entity.id,
            reportId = entity.reportId,
            photoId = entity.photoId,
            type = DefectType.valueOf(entity.type),
            severity = DefectSeverity.valueOf(entity.severity),
            description = entity.description,
            confidence = entity.confidence,
            isMLDetected = entity.isMLDetected,
            isVerified = entity.isVerified,
            createdAt = entity.createdAt
        )
    }
}