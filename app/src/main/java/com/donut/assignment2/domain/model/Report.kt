package com.donut.assignment2.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class Report(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val inspectorPhone: String = "",        // 🔥 Changed from inspectorId
    val status: ReportStatus = ReportStatus.DRAFT,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val submittedAt: LocalDateTime? = null,
    val reviewedAt: LocalDateTime? = null,
    val supervisorPhone: String? = null,    // 🔥 Changed from supervisorId
    val supervisorNotes: String = "",
    val photos: List<Photo> = emptyList(),
    val defects: List<Defect> = emptyList()
)

enum class ReportStatus {
    DRAFT,           // Inspector đang tạo
    SUBMITTED,       // Inspector đã submit
    UNDER_REVIEW,    // Supervisor đang review
    APPROVED,        // Supervisor đã approve
    REJECTED         // Supervisor đã reject
}
