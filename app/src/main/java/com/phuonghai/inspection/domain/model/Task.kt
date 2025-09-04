package com.phuonghai.inspection.domain.model

import com.google.firebase.Timestamp
import com.phuonghai.inspection.domain.common.Priority

data class Task(
    val taskId: String = "",
    val supervisorId: String = "",
    val inspectorId: String = "",
    val branchId: String = "",
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.NORMAL,
    val status:  TaskStatus = TaskStatus.ASSIGNED,
    val dueDate: Timestamp ? = null,
    val createdAt: Timestamp ? = null,
)
enum class TaskStatus {
    ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED, OVERDUE
}

