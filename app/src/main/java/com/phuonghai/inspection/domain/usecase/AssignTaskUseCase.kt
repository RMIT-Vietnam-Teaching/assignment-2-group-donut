package com.phuonghai.inspection.domain.usecase

import com.phuonghai.inspection.domain.model.Task
import com.phuonghai.inspection.domain.repository.ITaskRepository
import javax.inject.Inject

class AssignTaskUseCase @Inject constructor(
    private val taskRepository: ITaskRepository
){
    suspend operator fun invoke(task: Task): Result<Unit> {
        return taskRepository.createTask(task)
    }
}