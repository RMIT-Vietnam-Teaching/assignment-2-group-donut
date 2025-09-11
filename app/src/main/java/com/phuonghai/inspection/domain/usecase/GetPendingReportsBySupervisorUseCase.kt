package com.phuonghai.inspection.domain.usecase

import com.phuonghai.inspection.domain.repository.IReportRepository
import javax.inject.Inject

class GetPendingReportsBySupervisorUseCase @Inject constructor(
    private val reportRepository: IReportRepository
) {
    suspend operator fun invoke(supervisorId: String) =
        reportRepository.getPendingReportsBySupervisorId(supervisorId)
}
