package com.phuonghai.inspection.domain.usecase

import com.phuonghai.inspection.domain.model.Report
import com.phuonghai.inspection.domain.repository.IReportRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReportsByInspectorUseCase @Inject constructor(
    private val reportRepository: IReportRepository
) {
    operator fun invoke(inspectorId: String): Flow<List<Report>> {
        return reportRepository.getReportsByInspectorId(inspectorId)
    }
}
