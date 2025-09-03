package com.phuonghai.inspection.domain.repository

import com.phuonghai.inspection.domain.model.Task

interface ITaskRepository {
    suspend fun createTask(task: Task): Result<Unit>
    suspend fun getTask(taskId: String): Result<Task>
}