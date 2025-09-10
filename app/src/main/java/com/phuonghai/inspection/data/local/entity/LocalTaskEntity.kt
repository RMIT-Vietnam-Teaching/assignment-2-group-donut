package com.phuonghai.inspection.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.phuonghai.inspection.domain.model.Task
import com.phuonghai.inspection.domain.model.TaskStatus
import com.phuonghai.inspection.domain.common.Priority
import com.google.firebase.Timestamp

@Entity(tableName = "local_tasks")
data class LocalTaskEntity(
    @PrimaryKey
    val taskId: String,
    val supervisorId: String,
    val inspectorId: String,
    val branchId: String,
    val title: String,
    val description: String,
    val status: String, // TaskStatus enum as string
    val priority: String, // Priority enum as string
    val createdAt: Long?, // Timestamp in milliseconds
    val dueDate: Long?, // Optional due date

    // 🆕 Cache Management Fields
    val cacheTimestamp: Long = System.currentTimeMillis(),
    val needsSync: Boolean = false,
    val lastSyncAttempt: Long = 0L,
    val syncRetryCount: Int = 0,

    // 🆕 Local State Management
    val isDeleted: Boolean = false,
    val localModifiedAt: Long = System.currentTimeMillis(),

    // 🆕 Additional Metadata
    val localNotes: String = "", // Local notes that might not be synced yet
    val originalStatus: String = status, // To track status changes
    val isLocalOnly: Boolean = false, // For tasks created offline

    // 🆕 Additional fields for extended functionality (optional)
    val isDownloaded: Boolean = true, // Flag to track if task is downloaded for offline use
    val lastSyncAt: Long = System.currentTimeMillis() // Last time task was synced
)

// 🆕 Extension functions for conversion
fun Task.toLocalEntity(): LocalTaskEntity {
    return LocalTaskEntity(
        taskId = this.taskId,
        supervisorId = this.supervisorId,
        inspectorId = this.inspectorId,
        branchId = this.branchId,
        title = this.title,
        description = this.description,
        status = this.status.name,
        priority = this.priority.name,
        createdAt = this.createdAt?.seconds?.times(1000),
        dueDate = this.dueDate?.seconds?.times(1000),
        cacheTimestamp = System.currentTimeMillis(),
        needsSync = false, // Default to synced when coming from server
        originalStatus = this.status.name,
        isLocalOnly = false,
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
        status = try {
            TaskStatus.valueOf(this.status)
        } catch (e: IllegalArgumentException) {
            TaskStatus.ASSIGNED // Default fallback to match current enum
        },
        priority = try {
            Priority.valueOf(this.priority)
        } catch (e: IllegalArgumentException) {
            Priority.NORMAL // Default fallback to match current enum
        },
        createdAt = this.createdAt?.let { Timestamp(it / 1000, 0) },
        dueDate = this.dueDate?.let { Timestamp(it / 1000, 0) }
    )
}

// 🆕 Helper extension functions
fun LocalTaskEntity.isExpired(expiryHours: Int = 24): Boolean {
    val currentTime = System.currentTimeMillis()
    val expiryTime = expiryHours * 60 * 60 * 1000L
    return (currentTime - this.cacheTimestamp) > expiryTime
}

fun LocalTaskEntity.hasStatusChanged(): Boolean {
    return this.status != this.originalStatus
}

fun LocalTaskEntity.isOverdue(): Boolean {
    val currentTime = System.currentTimeMillis()
    return this.dueDate?.let { it < currentTime } ?: false
}

fun LocalTaskEntity.isPending(): Boolean {
    return this.status == TaskStatus.ASSIGNED.name // Use ASSIGNED instead of PENDING
}

fun LocalTaskEntity.isCompleted(): Boolean {
    return this.status == TaskStatus.COMPLETED.name
}

fun LocalTaskEntity.isInProgress(): Boolean {
    return this.status == TaskStatus.IN_PROGRESS.name
}

fun LocalTaskEntity.markAsNeedsSync(): LocalTaskEntity {
    return this.copy(
        needsSync = true,
        localModifiedAt = System.currentTimeMillis()
    )
}

fun LocalTaskEntity.markAsSynced(): LocalTaskEntity {
    return this.copy(
        needsSync = false,
        syncRetryCount = 0,
        lastSyncAttempt = 0L,
        originalStatus = this.status,
        lastSyncAt = System.currentTimeMillis()
    )
}

fun LocalTaskEntity.updateStatus(newStatus: TaskStatus): LocalTaskEntity {
    return this.copy(
        status = newStatus.name,
        needsSync = true,
        localModifiedAt = System.currentTimeMillis()
    )
}

// 🆕 Additional helper functions
fun LocalTaskEntity.incrementSyncRetry(): LocalTaskEntity {
    return this.copy(
        syncRetryCount = this.syncRetryCount + 1,
        lastSyncAttempt = System.currentTimeMillis()
    )
}

fun LocalTaskEntity.resetSync(): LocalTaskEntity {
    return this.copy(
        needsSync = false,
        syncRetryCount = 0,
        lastSyncAttempt = 0L,
        lastSyncAt = System.currentTimeMillis()
    )
}

fun LocalTaskEntity.isHighPriority(): Boolean {
    return this.priority == Priority.HIGH.name
}

fun LocalTaskEntity.isLowPriority(): Boolean {
    return this.priority == Priority.LOW.name
}

fun LocalTaskEntity.canBeDeleted(): Boolean {
    return !this.needsSync && this.syncRetryCount == 0
}