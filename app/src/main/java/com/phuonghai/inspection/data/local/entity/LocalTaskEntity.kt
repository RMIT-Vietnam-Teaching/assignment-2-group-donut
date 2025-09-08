package com.phuonghai.inspection.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.phuonghai.inspection.domain.model.Task
import com.phuonghai.inspection.domain.model.TaskStatus

@Entity(tableName = "local_tasks")
data class LocalTaskEntity(
    @PrimaryKey
    val taskId: String,
    val inspectorId: String,
    val branchId: String,
    val title: String,
    val description: String,
    val location: String,
    val lat: String,
    val lng: String,
    val status: String, // TaskStatus as String
    val priority: String,
    val dueDate: Long, // Timestamp as Long
    val createdAt: Long, // Timestamp as Long
    val assignedAt: Long?, // Timestamp as Long
    val completedAt: Long?, // Timestamp as Long
    val isDownloaded: Boolean = true, // Flag to track if task is downloaded for offline use
    val lastSyncAt: Long = System.currentTimeMillis() // Last time task was synced
)

// Extension functions to convert between domain model and entity
fun Task.toLocalEntity(): LocalTaskEntity {
    return LocalTaskEntity(
        taskId = this.taskId,
        inspectorId = this.inspectorId,
        branchId = this.branchId,
        title = this.title,
        description = this.description,
        location = this.location,
        lat = this.lat,
        lng = this.lng,
        status = this.status.name,
        priority = this.priority,
        dueDate = this.dueDate?.seconds?.times(1000) ?: System.currentTimeMillis(),
        createdAt = this.createdAt?.seconds?.times(1000) ?: System.currentTimeMillis(),
        assignedAt = this.assignedAt?.seconds?.times(1000),
        completedAt = this.completedAt?.seconds?.times(1000),
        isDownloaded = true,
        lastSyncAt = System.currentTimeMillis()
    )
}

fun LocalTaskEntity.toDomainModel(): Task {
    return Task(
        taskId = this.taskId,
        inspectorId = this.inspectorId,
        branchId = this.branchId,
        title = this.title,
        description = this.description,
        location = this.location,
        lat = this.lat,
        lng = this.lng,
        status = TaskStatus.valueOf(this.status),
        priority = this.priority,
        dueDate = com.google.firebase.Timestamp(this.dueDate / 1000, 0),
        createdAt = com.google.firebase.Timestamp(this.createdAt / 1000, 0),
        assignedAt = this.assignedAt?.let { com.google.firebase.Timestamp(it / 1000, 0) },
        completedAt = this.completedAt?.let { com.google.firebase.Timestamp(it / 1000, 0) }
    )
}