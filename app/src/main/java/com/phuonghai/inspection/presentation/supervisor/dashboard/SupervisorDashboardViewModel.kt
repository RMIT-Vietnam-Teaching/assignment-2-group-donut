package com.phuonghai.inspection.presentation.supervisor.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.phuonghai.inspection.domain.model.Notification
import com.phuonghai.inspection.domain.model.NotificationType
import com.phuonghai.inspection.domain.model.Report
import com.phuonghai.inspection.domain.model.ResponseStatus
import com.phuonghai.inspection.domain.model.TaskStatus
import com.phuonghai.inspection.domain.model.User
import com.phuonghai.inspection.domain.usecase.CreateNotificationUseCase
import com.phuonghai.inspection.domain.usecase.GetReportsBySupervisorUseCase
import com.phuonghai.inspection.domain.usecase.GetTaskIdByReportIdUseCase
import com.phuonghai.inspection.domain.usecase.GetUserInformationUseCase
import com.phuonghai.inspection.domain.usecase.UpdateResponseStatusReportUseCase
import com.phuonghai.inspection.domain.usecase.UpdateTaskStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.emptyList as emptyList1

@HiltViewModel
class SupervisorDashboardViewModel @Inject constructor(
    private val getReportsBySupervisorUseCase: GetReportsBySupervisorUseCase,
    private val getUserInformationUseCase: GetUserInformationUseCase,
    private val updateResponseStatusReportUseCase: UpdateResponseStatusReportUseCase,
    private val getTaskIdByReportIdUseCase: GetTaskIdByReportIdUseCase,
    private val updateTaskStatusUseCase: UpdateTaskStatusUseCase,
    private val createNotificationUseCase: CreateNotificationUseCase
) : ViewModel() {
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""


    private val _reports = MutableStateFlow<List<Report>>(emptyList1())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _statistic = MutableStateFlow<TeamStatistics?>(null)
    val statistic: StateFlow<TeamStatistics?> = _statistic.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadReports() {
        viewModelScope.launch {
            _isLoading.value = true
            getReportsBySupervisorUseCase(currentUserId)
                .onSuccess { reports ->

                    // 👇 Compute statistics
                    val stats = TeamStatistics(
                        pendingReviews = reports.count { it.responseStatus.name == "PENDING" },
                        approvedReports = reports.count { it.responseStatus.name == "APPROVED" },
                        rejectedReports = reports.count { it.responseStatus.name == "REJECTED" },
                        totalReports = reports.size
                    )
                    _statistic.value = stats
                    // assign only pending reports to _reports
                    _reports.value = reports.filter { it.responseStatus.name == "PENDING" }
                    _isLoading.value = false
                }
                .onFailure { e ->
                    Log.e("SupervisorDashboardViewModel", "Error loading reports", e)
                    _isLoading.value = false
                }
        }
    }
    fun loadUser() {
        viewModelScope.launch {
            getUserInformationUseCase(currentUserId)
                .onSuccess { user ->
                    _user.value = user
                    loadReports()
                }
                .onFailure { e ->
                    Log.e("SupervisorDashboardViewModel", "Error loading user", e)
                }
        }
    }
    fun approveReport(reportId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Update report response status
                val result = updateResponseStatusReportUseCase(reportId, ResponseStatus.APPROVED.name)
                result.getOrThrow() // throws if failed

                // 2. Get related taskId
                val taskIdResult = getTaskIdByReportIdUseCase(reportId)
                val taskId = taskIdResult.getOrThrow()

                // 3. Update task status
                val result2 = updateTaskStatusUseCase(taskId, TaskStatus.COMPLETED)
                result2.getOrThrow()

                // 4. Create notification
                val inspectorId = _reports.value.find { it.reportId == reportId }?.inspectorId ?: ""
                if (inspectorId.isNotBlank()) {
                    val notification = Notification(
                        id = reportId, // or UUID
                        title = "Report Approved",
                        message = "Your report has been approved 🎉",
                        date = Timestamp.now(),
                        senderId = currentUserId,
                        receiverId = inspectorId,
                        type = NotificationType.REPORT_ACCEPTED
                    )
                    createNotificationUseCase(notification)
                }

                // 4. Reload data if everything succeeded
                loadReports()
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("SupervisorDashboardViewModel", "Error approving report", e)
                _isLoading.value = false
            }
        }
    }
    fun rejectReport(reportId: String) {
        viewModelScope.launch {
            try {
                updateResponseStatusReportUseCase(reportId, ResponseStatus.REJECTED.name).getOrThrow()

                // Notify inspector
                val inspectorId = _reports.value.find { it.reportId == reportId }?.inspectorId ?: ""
                if (inspectorId.isNotBlank()) {
                    val notification = Notification(
                        id = reportId,
                        title = "Report Rejected",
                        message = "Your report has been rejected. Please review and resubmit.",
                        date = Timestamp.now(),
                        senderId = currentUserId,
                        receiverId = inspectorId,
                        type = NotificationType.REPORT_REJECTED
                    )
                    createNotificationUseCase(notification)
                }

                // Refresh reports
                loadReports()
            } catch (e: Exception) {
                Log.e("SupervisorDashboardViewModel", "Error rejecting report", e)
            }
        }
    }
}
data class TeamStatistics(
    val pendingReviews: Int = 0,
    val approvedReports: Int = 0,
    val rejectedReports: Int = 0,
    val totalReports: Int = 0
)