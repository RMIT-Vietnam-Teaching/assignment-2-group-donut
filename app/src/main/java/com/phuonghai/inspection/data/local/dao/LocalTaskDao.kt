package com.phuonghai.inspection.data.local.dao

import androidx.room.*
import com.phuonghai.inspection.data.local.entity.LocalTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalTaskDao {

    // ✅ Basic CRUD Operations
    @Query("SELECT * FROM local_tasks WHERE inspectorId = :inspectorId ORDER BY createdAt DESC")
    suspend fun getTasksByInspectorId(inspectorId: String): List<LocalTaskEntity>

    @Query("SELECT * FROM local_tasks WHERE taskId = :taskId")
    suspend fun getTaskById(taskId: String): LocalTaskEntity?

    @Query("SELECT * FROM local_tasks WHERE branchId = :branchId ORDER BY createdAt DESC")
    suspend fun getTasksByBranchId(branchId: String): List<LocalTaskEntity>

    @Query("SELECT COUNT(*) FROM local_tasks WHERE inspectorId = :inspectorId")
    suspend fun getTasksCount(inspectorId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: LocalTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<LocalTaskEntity>)

    @Update
    suspend fun updateTask(task: LocalTaskEntity)

    @Delete
    suspend fun deleteTask(task: LocalTaskEntity)

    @Query("DELETE FROM local_tasks WHERE taskId = :taskId")
    suspend fun deleteTaskById(taskId: String)

    // Cache Management Methods
    @Query("DELETE FROM local_tasks WHERE inspectorId = :inspectorId")
    suspend fun deleteTasksByInspectorId(inspectorId: String)

    @Query("UPDATE local_tasks SET status = :status WHERE taskId = :taskId")
    suspend fun updateTaskStatus(taskId: String, status: String)

    @Query("DELETE FROM local_tasks WHERE cacheTimestamp < :expiryTime")
    suspend fun deleteExpiredCache(expiryTime: Long)

    @Query("DELETE FROM local_tasks WHERE createdAt < :cutoffTime")
    suspend fun deleteOldTasks(cutoffTime: Long)

    @Query("SELECT * FROM local_tasks WHERE cacheTimestamp < :expiryTime")
    suspend fun getExpiredTasks(expiryTime: Long): List<LocalTaskEntity>

    // Sync Management Methods
    @Query("SELECT * FROM local_tasks WHERE inspectorId = :inspectorId AND needsSync = 1")
    suspend fun getUnsyncedTasksByInspector(inspectorId: String): List<LocalTaskEntity>

    @Query("SELECT * FROM local_tasks WHERE needsSync = 1 ORDER BY localModifiedAt ASC")
    suspend fun getAllUnsyncedTasks(): List<LocalTaskEntity>

    @Query("SELECT COUNT(*) FROM local_tasks WHERE needsSync = 1")
    suspend fun getUnsyncedTasksCount(): Int

    @Query("SELECT COUNT(*) FROM local_tasks WHERE inspectorId = :inspectorId AND needsSync = 1")
    suspend fun getUnsyncedTasksCountByInspector(inspectorId: String): Int

    @Query("UPDATE local_tasks SET needsSync = 0 WHERE taskId = :taskId")
    suspend fun markTaskAsSynced(taskId: String)

    @Query("UPDATE local_tasks SET needsSync = 1 WHERE taskId = :taskId")
    suspend fun markTaskAsNeedsSync(taskId: String)

    @Query("""
        UPDATE local_tasks 
        SET lastSyncAttempt = :timestamp, syncRetryCount = syncRetryCount + 1 
        WHERE taskId = :taskId
    """)
    suspend fun updateSyncAttempt(taskId: String, timestamp: Long)

    @Query("UPDATE local_tasks SET syncRetryCount = 0 WHERE taskId = :taskId")
    suspend fun resetSyncRetryCount(taskId: String)

    @Query("SELECT * FROM local_tasks WHERE needsSync = 1 AND syncRetryCount < 3")
    suspend fun getTasksForSync(): List<LocalTaskEntity>

    // Advanced Query Methods
    @Query("""
        SELECT * FROM local_tasks 
        WHERE inspectorId = :inspectorId 
        AND isDeleted = 0 
        ORDER BY 
            CASE WHEN status = 'ASSIGNED' THEN 1
                 WHEN status = 'IN_PROGRESS' THEN 2
                 WHEN status = 'COMPLETED' THEN 3
                 WHEN status = 'CANCELLED' THEN 4
                 WHEN status = 'OVERDUE' THEN 5
                 ELSE 6 END,
            createdAt DESC
    """)
    suspend fun getTasksByInspectorIdSorted(inspectorId: String): List<LocalTaskEntity>

    @Query("""
        SELECT * FROM local_tasks 
        WHERE inspectorId = :inspectorId 
        AND status IN (:statuses)
        AND isDeleted = 0
        ORDER BY createdAt DESC
    """)
    suspend fun getTasksByStatus(inspectorId: String, statuses: List<String>): List<LocalTaskEntity>

    @Query("""
        SELECT * FROM local_tasks 
        WHERE inspectorId = :inspectorId 
        AND dueDate IS NOT NULL
        AND dueDate <= :dueDateThreshold
        AND status != 'COMPLETED'
        AND isDeleted = 0
        ORDER BY dueDate ASC
    """)
    suspend fun getOverdueTasks(inspectorId: String, dueDateThreshold: Long): List<LocalTaskEntity>

    // Flow Methods for Reactive Updates
    @Query("SELECT * FROM local_tasks WHERE inspectorId = :inspectorId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun observeTasksByInspectorId(inspectorId: String): Flow<List<LocalTaskEntity>>

    @Query("SELECT COUNT(*) FROM local_tasks WHERE inspectorId = :inspectorId AND needsSync = 1")
    fun observeUnsyncedTasksCount(inspectorId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM local_tasks WHERE inspectorId = :inspectorId AND status = 'ASSIGNED' AND isDeleted = 0")
    fun observePendingTasksCount(inspectorId: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM local_tasks 
        WHERE inspectorId = :inspectorId 
        AND dueDate IS NOT NULL
        AND dueDate <= :currentTime 
        AND status != 'COMPLETED'
        AND isDeleted = 0
    """)
    fun observeOverdueTasksCount(inspectorId: String, currentTime: Long): Flow<Int>

    // Maintenance Methods
    @Query("""
        DELETE FROM local_tasks 
        WHERE inspectorId = :inspectorId 
        AND needsSync = 0 
        AND taskId NOT IN (
            SELECT taskId FROM local_tasks 
            WHERE inspectorId = :inspectorId 
            ORDER BY createdAt DESC 
            LIMIT :limit
        )
    """)
    suspend fun trimOldSyncedTasks(inspectorId: String, limit: Int)

    @Query("UPDATE local_tasks SET isDeleted = 1, localModifiedAt = :timestamp WHERE taskId = :taskId")
    suspend fun markTaskAsDeleted(taskId: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM local_tasks WHERE isDeleted = 1 AND localModifiedAt < :cutoffTime")
    suspend fun permanentlyDeleteOldDeletedTasks(cutoffTime: Long)

    // Batch Operations
    @Query("UPDATE local_tasks SET needsSync = 1, localModifiedAt = :timestamp WHERE inspectorId = :inspectorId")
    suspend fun markAllTasksAsNeedingSync(inspectorId: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE local_tasks SET cacheTimestamp = :timestamp WHERE inspectorId = :inspectorId")
    suspend fun updateCacheTimestampForInspector(inspectorId: String, timestamp: Long = System.currentTimeMillis())

    // Statistics Methods
    @Query("""
        SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN status = 'ASSIGNED' THEN 1 ELSE 0 END) as pending,
            SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as inProgress,
            SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed,
            SUM(CASE WHEN needsSync = 1 THEN 1 ELSE 0 END) as unsynced
        FROM local_tasks 
        WHERE inspectorId = :inspectorId AND isDeleted = 0
    """)
    suspend fun getTaskStatistics(inspectorId: String): TaskStatistics

    // Search and Filter Methods - Only existing fields
    @Query("""
        SELECT * FROM local_tasks 
        WHERE inspectorId = :inspectorId 
        AND (title LIKE :searchQuery OR description LIKE :searchQuery)
        AND isDeleted = 0
        ORDER BY createdAt DESC
    """)
    suspend fun searchTasks(inspectorId: String, searchQuery: String): List<LocalTaskEntity>

    @Query("""
        SELECT * FROM local_tasks 
        WHERE inspectorId = :inspectorId 
        AND priority = :priority
        AND isDeleted = 0
        ORDER BY createdAt DESC
    """)
    suspend fun getTasksByPriority(inspectorId: String, priority: String): List<LocalTaskEntity>

    @Query("""
        SELECT * FROM local_tasks 
        WHERE inspectorId = :inspectorId 
        AND createdAt IS NOT NULL
        AND createdAt BETWEEN :startDate AND :endDate
        AND isDeleted = 0
        ORDER BY createdAt DESC
    """)
    suspend fun getTasksByDateRange(inspectorId: String, startDate: Long, endDate: Long): List<LocalTaskEntity>

    // Performance Optimization Methods
    @Query("SELECT taskId, title, status, priority FROM local_tasks WHERE inspectorId = :inspectorId AND isDeleted = 0")
    suspend fun getTaskSummaries(inspectorId: String): List<TaskSummary>

    @Query("SELECT COUNT(*) FROM local_tasks WHERE inspectorId = :inspectorId AND status = :status AND isDeleted = 0")
    suspend fun getTaskCountByStatus(inspectorId: String, status: String): Int

    // Utility Methods
    @Query("SELECT MAX(localModifiedAt) FROM local_tasks WHERE inspectorId = :inspectorId")
    suspend fun getLastModifiedTimestamp(inspectorId: String): Long?

    @Query("SELECT MIN(cacheTimestamp) FROM local_tasks WHERE inspectorId = :inspectorId")
    suspend fun getOldestCacheTimestamp(inspectorId: String): Long?

    // Data classes for return types
    data class TaskStatistics(
        val total: Int,
        val pending: Int,
        val inProgress: Int,
        val completed: Int,
        val unsynced: Int
    )

    data class TaskSummary(
        val taskId: String,
        val title: String,
        val status: String,
        val priority: String
    )
}