package com.phuonghai.inspection.domain.usecase

import com.phuonghai.inspection.domain.repository.IReportRepository
import javax.inject.Inject

class GetReportUseCase @Inject constructor(
    private val reportRepository: IReportRepository
) {
    suspend operator fun invoke(reportId:String) = reportRepository.getReport(reportId)
}