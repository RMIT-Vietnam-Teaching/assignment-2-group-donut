package com.phuonghai.inspection.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.phuonghai.inspection.domain.common.Priority
import com.phuonghai.inspection.domain.model.*

@Entity(tableName = "local_reports")
data class LocalReportEntity(
    @PrimaryKey
    val reportId: String,
    val inspectorId: String,
    val taskId: String,
    val title: String,
    val description: String,
    val type: String, // InspectionType as String
    val lat: String,
    val lng: String,
    val address: String,
    val score: Int?,
    val priority: String, // Priority as String
    val assignStatus: String, // AssignStatus as String
    val responseStatus: String, // ResponseStatus as String
    val syncStatus: String, // SyncStatus as String
    val imageUrl: String,
    val videoUrl: String,
    val reviewNotes: String,
    val reviewedBy: String,
    val createdAt: Long, // Timestamp as Long
    val completedAt: Long?, // Timestamp as Long
    val localImagePath: String = "", // Path to local image file
    val localVideoPath: String = "", // Path to local video file
    val needsSync: Boolean = true, // Flag to track if needs sync
    val lastSyncAttempt: Long = 0L, // Last sync attempt timestamp
    val syncRetryCount: Int = 0 // Number of retry attempts
)

// Extension functions to convert between domain model and entity
fun Report.toLocalEntity(): LocalReportEntity {
    return LocalReportEntity(
        reportId = this.reportId,
        inspectorId = this.inspectorId,
        taskId = this.taskId,
        title = this.title,
        description = this.description,
        type = this.type.name,
        lat = this.lat,
        lng = this.lng,
        address = this.address,
        score = this.score,
        priority = this.priority.name,
        assignStatus = this.assignStatus.name,
        responseStatus = this.responseStatus.name,
        syncStatus = this.syncStatus.name,
        imageUrl = this.imageUrl,
        videoUrl = this.videoUrl,
        reviewNotes = this.reviewNotes,
        reviewedBy = this.reviewedBy,
        createdAt = this.createdAt?.seconds?.times(1000) ?: System.currentTimeMillis(),
        completedAt = this.completedAt?.seconds?.times(1000),
        needsSync = this.syncStatus == SyncStatus.UNSYNCED
    )
}

fun LocalReportEntity.toDomainModel(): Report {
    return Report(
        reportId = this.reportId,
        inspectorId = this.inspectorId,
        taskId = this.taskId,
        title = this.title,
        description = this.description,
        type = InspectionType.valueOf(this.type),
        lat = this.lat,
        lng = this.lng,
        address = this.address,
        score = this.score,
        priority = Priority.valueOf(this.priority),
        assignStatus = AssignStatus.valueOf(this.assignStatus),
        responseStatus = ResponseStatus.valueOf(this.responseStatus),
        syncStatus = SyncStatus.valueOf(this.syncStatus),
        imageUrl = this.imageUrl,
        videoUrl = this.videoUrl,
        reviewNotes = this.reviewNotes,
        reviewedBy = this.reviewedBy,
        createdAt = com.google.firebase.Timestamp(this.createdAt / 1000, 0),
        completedAt = this.completedAt?.let { com.google.firebase.Timestamp(it / 1000, 0) }
    )
}