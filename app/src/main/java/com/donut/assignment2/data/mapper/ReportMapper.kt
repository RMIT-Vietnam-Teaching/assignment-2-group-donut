package com.donut.assignment2.data.mapper

import com.donut.assignment2.data.local.entities.ReportEntity
import com.donut.assignment2.domain.model.Report
import com.donut.assignment2.domain.model.ReportStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportMapper @Inject constructor() {

    fun toEntity(report: Report): ReportEntity {
        return ReportEntity(
            id = report.id,
            title = report.title,
            description = report.description,
            location = report.location,
            inspectorId = report.inspectorId,
            status = report.status.name,
            createdAt = report.createdAt,
            updatedAt = report.updatedAt,
            submittedAt = report.submittedAt,
            reviewedAt = report.reviewedAt,
            supervisorId = report.supervisorId,
            supervisorNotes = report.supervisorNotes
        )
    }

    fun fromEntity(entity: ReportEntity): Report {
        return Report(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            location = entity.location,
            inspectorId = entity.inspectorId,
            status = ReportStatus.valueOf(entity.status),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            submittedAt = entity.submittedAt,
            reviewedAt = entity.reviewedAt,
            supervisorId = entity.supervisorId,
            supervisorNotes = entity.supervisorNotes
        )
    }

    fun fromEntities(entities: List<ReportEntity>): List<Report> {
        return entities.map { fromEntity(it) }
    }
}