package com.phuonghai.inspection.domain.usecase

import com.phuonghai.inspection.domain.model.Task
import com.phuonghai.inspection.domain.repository.ITaskRepository
import javax.inject.Inject

class GetInspectorTasksUseCase @Inject constructor(
    private val taskRepository: ITaskRepository
) {
    suspend operator fun invoke(inspectorId: String): Result<List<Task>> {
        return taskRepository.getTasksByInspectorId(inspectorId)
    }
}