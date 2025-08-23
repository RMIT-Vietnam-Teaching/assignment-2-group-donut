package com.donut.assignment2.domain.usecase.inspector

import com.donut.assignment2.domain.model.Report
import com.donut.assignment2.domain.repository.ReportRepository
import javax.inject.Inject

class CreateReportUseCase @Inject constructor(
    private val reportRepository: ReportRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String,
        location: String,
        inspectorPhone: String    // 🔥 Phone parameter
    ): Result<String> {
        val report = Report(
            title = title.trim(),
            description = description.trim(),
            location = location.trim(),
            inspectorPhone = inspectorPhone  // 🔥 Store phone
        )

        return reportRepository.createReport(report)
    }
}