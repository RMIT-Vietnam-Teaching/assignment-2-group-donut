package com.donut.assignment2.domain.usecase.inspector

import com.donut.assignment2.domain.model.Report
import com.donut.assignment2.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMyReportsUseCase @Inject constructor(
    private val reportRepository: ReportRepository
) {
    operator fun invoke(inspectorId: String): Flow<List<Report>> {
        return reportRepository.getReportsByInspectorFlow(inspectorId)
    }
}
