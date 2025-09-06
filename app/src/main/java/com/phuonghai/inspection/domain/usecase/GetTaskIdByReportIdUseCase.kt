package com.phuonghai.inspection.domain.usecase

import com.phuonghai.inspection.domain.repository.ITaskRepository
import javax.inject.Inject

class GetTaskIdByReportIdUseCase @Inject constructor(
    private val taskRepository: ITaskRepository
) {
    suspend operator fun invoke(reportId: String): Result<String> {
        return taskRepository.getTaskIdByReportId(reportId)
    }
}