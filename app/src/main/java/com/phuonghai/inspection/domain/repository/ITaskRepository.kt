package com.phuonghai.inspection.domain.repository

import com.phuonghai.inspection.domain.model.Task
import com.phuonghai.inspection.domain.model.TaskStatus

interface ITaskRepository {
    suspend fun createTask(task: Task): Result<Unit>
    suspend fun getTask(taskId: String): Result<Task>
    suspend fun getTasksByInspectorId(inspectorId: String): Result<List<Task>>
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit>
    suspend fun getTasksByBranch(branchId: String): Result<List<Task>>
    suspend fun getTaskIdByReportId(reportId: String): Result<String>
}