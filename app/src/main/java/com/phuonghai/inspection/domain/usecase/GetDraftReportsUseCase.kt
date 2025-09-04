package com.phuonghai.inspection.domain.usecase

import com.phuonghai.inspection.domain.model.Report
import com.phuonghai.inspection.domain.repository.IReportRepository
import javax.inject.Inject

class GetDraftReportsUseCase @Inject constructor(
    private val reportRepository: IReportRepository
) {
    suspend operator fun invoke(inspectorId: String): Result<List<Report>> {
        return reportRepository.getDraftReportsByInspectorId(inspectorId)
    }
}