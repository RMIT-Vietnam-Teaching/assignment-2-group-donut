package com.phuonghai.inspection.presentation.supervisor.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.phuonghai.inspection.domain.model.*
import com.phuonghai.inspection.domain.repository.IReportRepository
import com.phuonghai.inspection.domain.repository.ITaskRepository
import com.phuonghai.inspection.domain.repository.IUserRepository
import com.phuonghai.inspection.domain.usecase.CreateNotificationUseCase
import com.phuonghai.inspection.domain.usecase.GetPendingReportsBySupervisorUseCase
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

// Data class mới để biểu diễn trạng thái của Inspector
data class InspectorStatus(
    val inspector: User,
    val isWorking: Boolean // true nếu có task IN_PROGRESS
)

@HiltViewModel
class SupervisorDashboardViewModel @Inject constructor(
    private val getPendingReportsBySupervisorUseCase: GetPendingReportsBySupervisorUseCase,
    private val getUserInformationUseCase: GetUserInformationUseCase,
    private val updateResponseStatusReportUseCase: UpdateResponseStatusReportUseCase,
    private val getTaskIdByReportIdUseCase: GetTaskIdByReportIdUseCase,
    private val updateTaskStatusUseCase: UpdateTaskStatusUseCase,
    private val createNotificationUseCase: CreateNotificationUseCase,
    private val userRepository: IUserRepository,
    private val taskRepository: ITaskRepository
) : ViewModel() {
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _statistic = MutableStateFlow<TeamStatistics?>(null)
    val statistic: StateFlow<TeamStatistics?> = _statistic.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _inspectorStatuses = MutableStateFlow<List<InspectorStatus>>(emptyList())
    val inspectorStatuses: StateFlow<List<InspectorStatus>> = _inspectorStatuses.asStateFlow()

    init {
        loadAllDashboardData()
    }

    fun loadAllDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            getUserInformationUseCase(currentUserId).onSuccess { supervisor ->
                _user.value = supervisor
            }.onFailure { e ->
                Log.e("SupervisorDashboardVM", "Error loading user", e)
            }

            loadPendingReports()
            loadInspectorStatuses()

            _isLoading.value = false
        }
    }

    private fun loadPendingReports() {
        viewModelScope.launch {
            getPendingReportsBySupervisorUseCase(currentUserId)
                .onSuccess { reports ->
                    val stats = TeamStatistics(
                        pendingReviews = reports.size,
                        approvedReports = 0,
                        rejectedReports = 0,
                        totalReports = reports.size
                    )
                    _statistic.value = stats
                    _reports.value = reports
                }
                .onFailure { e ->
                    Log.e("SupervisorDashboardVM", "Error loading reports", e)
                }
        }
    }

    private fun loadInspectorStatuses() {
        viewModelScope.launch {
            try {
                val inspectorsResult = userRepository.getInspectors()
                inspectorsResult.getOrNull()?.let { allInspectors ->
                    val myInspectors = allInspectors.filter { it.supervisorId == currentUserId }

                    val statusList = mutableListOf<InspectorStatus>()

                    myInspectors.forEach { inspector ->
                        val tasksResult = taskRepository.getTasksByInspectorId(inspector.uId)
                        tasksResult.getOrNull()?.let { tasks ->
                            val isWorking = tasks.any { it.status == TaskStatus.IN_PROGRESS }
                            statusList.add(InspectorStatus(inspector, isWorking))
                        }
                    }
                    _inspectorStatuses.value = statusList
                }
            } catch (e: Exception) {
                Log.e("SupervisorDashboardVM", "Error loading inspector statuses", e)
            }
        }
    }

    fun approveReport(reportId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = updateResponseStatusReportUseCase(reportId, ResponseStatus.APPROVED.name)
                result.getOrThrow()

                val taskIdResult = getTaskIdByReportIdUseCase(reportId)
                val taskId = taskIdResult.getOrThrow()

                val result2 = updateTaskStatusUseCase(taskId, TaskStatus.COMPLETED)
                result2.getOrThrow()

                val inspectorId = _reports.value.find { it.reportId == reportId }?.inspectorId ?: ""
                if (inspectorId.isNotBlank()) {
                    val notification = Notification(
                        id = reportId,
                        title = "Report Approved",
                        message = "Your report has been approved 🎉",
                        date = Timestamp.now(),
                        senderId = currentUserId,
                        receiverId = inspectorId,
                        type = NotificationType.REPORT_ACCEPTED
                    )
                    createNotificationUseCase(notification)
                }

                loadAllDashboardData()
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

                loadAllDashboardData()
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