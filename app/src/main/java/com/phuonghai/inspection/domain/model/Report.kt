package com.phuonghai.inspection.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Report(
    val reportId: String = "",
    val inspectorId: String = "",
    val taskId: String = "",
    val title: String = "",
    val description: String = "",
    val type: InspectionType = InspectionType.ELECTRICAL,
    val lat: String = "",
    val lng: String = "",
    val address: String = "",
    val score: Int? = null,
    val priority: Priority = Priority.NORMAL,
    val assignStatus: AssignStatus = AssignStatus.PENDING_REVIEW,
    val responseStatus: ResponseStatus = ResponseStatus.PENDING,
    val syncStatus: SyncStatus = SyncStatus.UNSYNCED,
    val imageUrls: List<String> = emptyList(),
    val reviewNotes: String = "",
    val reviewedBy: String = "",
    val createdAt: Timestamp? = null,
    val completedAt: Timestamp? = null
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

enum class Priority {
    HIGH, NORMAL, LOW
}