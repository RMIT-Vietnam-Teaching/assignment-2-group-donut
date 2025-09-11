package com.phuonghai.inspection.domain.model

import com.google.firebase.Timestamp

enum class NotificationType {
    TASK_ASSIGNED,
    REPORT_ACCEPTED,
    REPORT_REJECTED
}

data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val date: Timestamp? = null,
    val senderId: String = "",
    val receiverId: String = "",
    val type: NotificationType = NotificationType.TASK_ASSIGNED,
)