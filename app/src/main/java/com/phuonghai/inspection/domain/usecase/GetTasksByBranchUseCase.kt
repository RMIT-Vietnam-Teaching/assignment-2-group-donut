package com.phuonghai.inspection.domain.usecase

import com.phuonghai.inspection.domain.repository.ITaskRepository
import javax.inject.Inject

class GetTasksByBranchUseCase @Inject constructor(
    private val taskRepository: ITaskRepository
) {
    suspend operator fun invoke(branchId: String) = taskRepository.getTasksByBranch(branchId)
}