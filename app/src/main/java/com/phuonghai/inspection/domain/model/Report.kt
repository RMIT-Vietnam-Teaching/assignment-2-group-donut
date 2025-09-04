package com.phuonghai.inspection.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.phuonghai.inspection.domain.common.Priority

@IgnoreExtraProperties
data class Report(
    val reportId: String = "",
    val inspectorId: String = "", // Who created the report
    val taskId: String = "", // Link to task
    val title: String = "", // Report title/summary
    val description: String = "", // Detailed notes
    val type: InspectionType = InspectionType.ELECTRICAL,
    val lat: String = "",
    val lng: String = "",
    val address: String = "", // Human readable address
    val score: Int? = null, // Numerical score (0-100)
    val priority: Priority = Priority.NORMAL, // HIGH, NORMAL, LOW
    val assignStatus: AssignStatus = AssignStatus.PENDING_REVIEW,
    val responseStatus: ResponseStatus = ResponseStatus.PENDING,
    val syncStatus: SyncStatus = SyncStatus.UNSYNCED,

    val imageUrl: String = "", // Single image URL
    val videoUrl: String = "", // Single video URL

    val reviewNotes: String = "", // Supervisor's review comments
    val reviewedBy: String = "", // Supervisor who reviewed
    val createdAt: Timestamp? = null,
    val completedAt: Timestamp? = null // When inspection finished
)

enum class InspectionType {
    ELECTRICAL, FIRE_SAFETY, STRUCTURAL, FOOD_HYGIENE, ENVIRONMENTAL, MACHINERY
}

enum class AssignStatus {
    DRAFT, PENDING_REVIEW, PASSED, FAILED, NEEDS_ATTENTION
}

enum class ResponseStatus {
    PENDING, APPROVED, REJECTED
}

enum class SyncStatus {
    SYNCED, UNSYNCED
}

