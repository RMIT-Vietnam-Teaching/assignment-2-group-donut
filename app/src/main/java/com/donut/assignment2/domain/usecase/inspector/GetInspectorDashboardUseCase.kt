package com.donut.assignment2.domain.usecase.inspector

import com.donut.assignment2.domain.model.InspectorDashboard
import com.donut.assignment2.domain.model.Report
import com.donut.assignment2.domain.model.ReportStatus
import com.donut.assignment2.domain.model.User
import com.donut.assignment2.domain.repository.ReportRepository
import com.donut.assignment2.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetInspectorDashboardUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val reportRepository: ReportRepository
) {
    suspend operator fun invoke(inspectorPhone: String): Result<InspectorDashboard> {
        // 🔥 Use phone to get user
        val user = userRepository.getUserByPhone(inspectorPhone)
            ?: return Result.failure(Exception("User not found"))

        // 🔥 Use phone to get reports
        val reportsResult = reportRepository.getReportsByInspector(inspectorPhone)

        return reportsResult.map { reports ->
            InspectorDashboard(
                user = user,
                draftReports = reports.count { it.status == ReportStatus.DRAFT },
                submittedReports = reports.count { it.status == ReportStatus.SUBMITTED },
                approvedReports = reports.count { it.status == ReportStatus.APPROVED },
                rejectedReports = reports.count { it.status == ReportStatus.REJECTED },
                recentReports = reports.take(5)
            )
        }
    }
}