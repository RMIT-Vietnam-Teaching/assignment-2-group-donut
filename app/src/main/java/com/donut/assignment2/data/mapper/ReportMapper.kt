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
            inspectorPhone = report.inspectorPhone,
            status = report.status.name,
            createdAt = report.createdAt,
            updatedAt = report.updatedAt,
            submittedAt = report.submittedAt,
            reviewedAt = report.reviewedAt,
            supervisorPhone = report.supervisorPhone,
            supervisorNotes = report.supervisorNotes
        )
    }

    fun fromEntity(entity: ReportEntity): Report {
        return Report(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            location = entity.location,
            inspectorPhone = entity.inspectorPhone,
            status = ReportStatus.valueOf(entity.status),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            submittedAt = entity.submittedAt,
            reviewedAt = entity.reviewedAt,
            supervisorPhone = entity.supervisorPhone,
            supervisorNotes = entity.supervisorNotes
        )
    }

    fun fromEntities(entities: List<ReportEntity>): List<Report> {
        return entities.map { fromEntity(it) }
    }

    // 🆕 Helper function to convert Report to Firestore map
    fun toFirestoreMap(report: Report): Map<String, Any?> {
        return hashMapOf(
            "id" to report.id,
            "title" to report.title,
            "description" to report.description,
            "location" to report.location,
            "inspectorPhone" to report.inspectorPhone,
            "status" to report.status.name,
            "createdAt" to report.createdAt.toString(), // Convert LocalDateTime to String
            "updatedAt" to report.updatedAt.toString(),
            "submittedAt" to report.submittedAt?.toString(),
            "reviewedAt" to report.reviewedAt?.toString(),
            "supervisorPhone" to report.supervisorPhone,
            "supervisorNotes" to report.supervisorNotes
        )
    }

    // 🆕 Helper function to convert Firestore data to Report
    fun fromFirestoreMap(data: Map<String, Any>, documentId: String): Report {
        return Report(
            id = data["id"] as? String ?: documentId,
            title = data["title"] as? String ?: "",
            description = data["description"] as? String ?: "",
            location = data["location"] as? String ?: "",
            inspectorPhone = data["inspectorPhone"] as? String ?: "",
            status = try {
                ReportStatus.valueOf(data["status"] as? String ?: "DRAFT")
            } catch (e: IllegalArgumentException) {
                ReportStatus.DRAFT
            },
            createdAt = parseDateTime(data["createdAt"] as? String),
            updatedAt = parseDateTime(data["updatedAt"] as? String),
            submittedAt = parseDateTimeNullable(data["submittedAt"] as? String),
            reviewedAt = parseDateTimeNullable(data["reviewedAt"] as? String),
            supervisorPhone = data["supervisorPhone"] as? String,
            supervisorNotes = data["supervisorNotes"] as? String ?: ""
        )
    }

    // 🔧 Helper function to parse LocalDateTime from String
    private fun parseDateTime(dateTimeString: String?): java.time.LocalDateTime {
        return try {
            dateTimeString?.let {
                java.time.LocalDateTime.parse(it)
            } ?: java.time.LocalDateTime.now()
        } catch (e: Exception) {
            java.time.LocalDateTime.now()
        }
    }

    // 🔧 Helper function to parse nullable LocalDateTime from String
    private fun parseDateTimeNullable(dateTimeString: String?): java.time.LocalDateTime? {
        return try {
            dateTimeString?.let {
                java.time.LocalDateTime.parse(it)
            }
        } catch (e: Exception) {
            null
        }
    }
}