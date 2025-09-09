package com.phuonghai.inspection.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.phuonghai.inspection.domain.common.Priority
import com.phuonghai.inspection.domain.model.Task
import com.phuonghai.inspection.domain.model.TaskStatus

@Entity(tableName = "local_tasks")
data class LocalTaskEntity(
    @PrimaryKey
    val taskId: String,
    val supervisorId: String,
    val inspectorId: String,
    val branchId: String,
    val title: String,
    val description: String,
    val priority: String, // Priority as String
    val status: String, // TaskStatus as String
    val dueDate: Long?, // Timestamp as Long (nullable)
    val createdAt: Long?, // Timestamp as Long (nullable)
    val isDownloaded: Boolean = true, // Flag to track if task is downloaded for offline use
    val lastSyncAt: Long = System.currentTimeMillis() // Last time task was synced
)

// Extension functions to convert between domain model and entity
fun Task.toLocalEntity(): LocalTaskEntity {
    return LocalTaskEntity(
        taskId = this.taskId,
        supervisorId = this.supervisorId,
        inspectorId = this.inspectorId,
        branchId = this.branchId,
        title = this.title,
        description = this.description,
        priority = this.priority.name,
        status = this.status.name,
        dueDate = this.dueDate?.seconds?.times(1000),
        createdAt = this.createdAt?.seconds?.times(1000),
        isDownloaded = true,
        lastSyncAt = System.currentTimeMillis()
    )
}

fun LocalTaskEntity.toDomainModel(): Task {
    return Task(
        taskId = this.taskId,
        supervisorId = this.supervisorId,
        inspectorId = this.inspectorId,
        branchId = this.branchId,
        title = this.title,
        description = this.description,
        priority = Priority.valueOf(this.priority),
        status = TaskStatus.valueOf(this.status),
        dueDate = this.dueDate?.let { com.google.firebase.Timestamp(it / 1000, 0) },
        createdAt = this.createdAt?.let { com.google.firebase.Timestamp(it / 1000, 0) }
    )
}