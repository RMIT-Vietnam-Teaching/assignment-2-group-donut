package com.donut.assignment2.domain.model

import java.time.LocalDateTime

data class Report(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val inspectorId: String = "",
    val status: ReportStatus = ReportStatus.DRAFT,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val submittedAt: LocalDateTime? = null,
    val reviewedAt: LocalDateTime? = null,
    val supervisorId: String? = null,
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
