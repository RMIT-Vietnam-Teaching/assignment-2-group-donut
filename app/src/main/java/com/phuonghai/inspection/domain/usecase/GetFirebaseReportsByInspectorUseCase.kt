package com.phuonghai.inspection.domain.usecase

import com.phuonghai.inspection.data.repository.ReportRepositoryImpl
import com.phuonghai.inspection.domain.model.Report
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFirebaseReportsByInspectorUseCase @Inject constructor(
    private val firebaseReportRepository: ReportRepositoryImpl
) {
    operator fun invoke(inspectorId: String): Flow<List<Report>> {
        return firebaseReportRepository.getReportsByInspectorId(inspectorId)
    }
}