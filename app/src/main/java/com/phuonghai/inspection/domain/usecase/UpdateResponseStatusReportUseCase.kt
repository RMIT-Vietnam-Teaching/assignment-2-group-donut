package com.phuonghai.inspection.domain.usecase

import com.phuonghai.inspection.domain.repository.IReportRepository
import javax.inject.Inject

class UpdateResponseStatusReportUseCase @Inject constructor(
    private val reportRepository: IReportRepository
){
    suspend operator fun invoke(reportId: String, status: String): Result<Unit> {
        return reportRepository.updateStatus(reportId, status)
    }
}