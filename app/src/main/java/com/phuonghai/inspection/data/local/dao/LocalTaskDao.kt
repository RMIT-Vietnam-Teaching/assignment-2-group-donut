package com.phuonghai.inspection.data.local.dao

import androidx.room.*
import com.phuonghai.inspection.data.local.entity.LocalTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalTaskDao {

    @Query("SELECT * FROM local_tasks WHERE inspectorId = :inspectorId ORDER BY dueDate ASC")
    fun getTasksByInspectorId(inspectorId: String): Flow<List<LocalTaskEntity>>

    @Query("SELECT * FROM local_tasks WHERE inspectorId = :inspectorId ORDER BY dueDate ASC")
    suspend fun getTasksByInspectorIdSync(inspectorId: String): List<LocalTaskEntity>

    @Query("SELECT * FROM local_tasks WHERE taskId = :taskId")
    suspend fun getTaskById(taskId: String): LocalTaskEntity?

    @Query("SELECT * FROM local_tasks WHERE branchId = :branchId")
    suspend fun getTasksByBranchId(branchId: String): List<LocalTaskEntity>

    @Query("SELECT * FROM local_tasks WHERE status = :status")
    suspend fun getTasksByStatus(status: String): List<LocalTaskEntity>

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

    @Query("UPDATE local_tasks SET status = :status WHERE taskId = :taskId")
    suspend fun updateTaskStatus(taskId: String, status: String)

    @Query("UPDATE local_tasks SET lastSyncAt = :timestamp WHERE taskId = :taskId")
    suspend fun updateLastSyncTime(taskId: String, timestamp: Long)

    @Query("DELETE FROM local_tasks WHERE lastSyncAt < :cutoffTime")
    suspend fun deleteOldTasks(cutoffTime: Long)

    @Query("SELECT COUNT(*) FROM local_tasks WHERE inspectorId = :inspectorId")
    suspend fun getTasksCount(inspectorId: String): Int

    @Query("SELECT * FROM local_tasks WHERE inspectorId = :inspectorId AND status IN ('ASSIGNED', 'IN_PROGRESS')")
    suspend fun getActiveTasks(inspectorId: String): List<LocalTaskEntity>
}