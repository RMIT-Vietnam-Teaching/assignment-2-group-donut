package com.phuonghai.inspection.domain.usecase

import com.phuonghai.inspection.domain.repository.IReportRepository
import javax.inject.Inject

class GetReportsBySupervisorUseCase @Inject constructor(
    private val reportRepository: IReportRepository
) {
    suspend operator fun invoke(supervisorId: String) = reportRepository.getReportsBySupervisorId(supervisorId)

}