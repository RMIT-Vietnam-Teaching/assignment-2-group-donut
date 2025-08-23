package com.example.supervisor_ui.data

data class InspectionItem(
    val id: Int,
    val title: String,
    val description: String,
    val type: String,
    val status: InspectionStatus,
    val date: String,
    val location: String,
    val action: InspectionAction
)

enum class InspectionStatus {
    PENDING_REVIEW,
    PASSED,
    FAILED,
    NEEDS_ATTENTION
}

enum class InspectionAction {
    APPROVE,
    REJECT,
    NONE
}