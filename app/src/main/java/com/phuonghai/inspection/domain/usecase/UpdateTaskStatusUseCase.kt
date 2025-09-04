package com.phuonghai.inspection.domain.usecase

import com.phuonghai.inspection.domain.model.TaskStatus
import com.phuonghai.inspection.domain.repository.ITaskRepository
import javax.inject.Inject

class UpdateTaskStatusUseCase @Inject constructor(
    private val taskRepository: ITaskRepository
) {
    suspend operator fun invoke(taskId: String, status: TaskStatus): Result<Unit> {
        return taskRepository.updateTaskStatus(taskId, status)
    }
}