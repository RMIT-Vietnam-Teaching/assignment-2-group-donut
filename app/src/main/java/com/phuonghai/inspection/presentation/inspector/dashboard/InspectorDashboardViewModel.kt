package com.phuonghai.inspection.presentation.home.inspector

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.phuonghai.inspection.core.network.NetworkMonitor
import com.phuonghai.inspection.domain.model.Report
import com.phuonghai.inspection.domain.model.ResponseStatus
import com.phuonghai.inspection.domain.model.Task
import com.phuonghai.inspection.domain.model.TaskStatus
import com.phuonghai.inspection.domain.model.User
import com.phuonghai.inspection.domain.usecase.GetInspectorTasksUseCase
import com.phuonghai.inspection.domain.usecase.GetUserInformationUseCase
import com.phuonghai.inspection.domain.usecase.GetReportsByInspectorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class InspectorDashboardViewModel @Inject constructor(
    private val getUserInformationUseCase: GetUserInformationUseCase,
    private val getInspectorTasksUseCase: GetInspectorTasksUseCase,
    private val getReportsByInspectorUseCase: GetReportsByInspectorUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(InspectorDashboardUiState())
    val uiState: StateFlow<InspectorDashboardUiState> = _uiState.asStateFlow()
    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        observeNetworkStatus()
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            networkMonitor.isConnected.collect { isConnected ->
                _isOnline.value = isConnected
            }
        }
    }
    companion object {
        private const val TAG = "InspectorDashboardViewModel"
    }

    fun loadDashboardData() {
        Log.d(TAG, "Loading dashboard data for user: $currentUserId")

        if (currentUserId.isEmpty()) {
            Log.e(TAG, "User not authenticated")
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Người dùng chưa được xác thực"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Load user information first
                loadUserInformation()

                // Load reports and tasks data (these use Flow so they will update automatically)
                loadReportsData()
                loadTodayTasks()

                Log.d(TAG, "Dashboard data loading initiated successfully")
                _uiState.value = _uiState.value.copy(isLoading = false)

            } catch (e: Exception) {
                Log.e(TAG, "Error loading dashboard data", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi tải dữ liệu: ${e.message}"
                )
            }
        }
    }

    private suspend fun loadUserInformation() {
        try {
            val result = getUserInformationUseCase(currentUserId)
            result.fold(
                onSuccess = { user ->
                    if (user != null) {
                        _uiState.value = _uiState.value.copy(currentUser = user)
                        Log.d(TAG, "User information loaded: ${user.fullName}")
                    } else {
                        Log.w(TAG, "User information not found")
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error loading user information", exception)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception loading user information", e)
        }
    }

    private suspend fun loadReportsData() {
        try {
            // Use Flow to get reports by inspector ID - this will automatically update when data changes
            viewModelScope.launch {
                getReportsByInspectorUseCase(currentUserId).collect { reports ->
                    Log.d(TAG, "Received ${reports.size} reports from Firebase for user: $currentUserId")

                    // Filter pending reports
                    val pendingReports = reports.filter { report ->
                        report.responseStatus == ResponseStatus.PENDING
                    }.map { report ->
                        ReportItem(
                            id = report.reportId,
                            title = report.title.ifEmpty { "Báo cáo không có tiêu đề" },
                            status = report.responseStatus.name,
                            createdAt = formatTimestamp(report.createdAt)
                        )
                    }

                    // Calculate statistics
                    val approvedReports = reports.count { it.responseStatus == ResponseStatus.APPROVED }
                    val pendingReportsCount = reports.count { it.responseStatus == ResponseStatus.PENDING }
                    val rejectedReports = reports.count { it.responseStatus == ResponseStatus.REJECTED }
                    val draftReports = reports.count { it.assignStatus.name == "DRAFT" }

                    val statistics = DashboardStatistics(
                        approvedReports = approvedReports,
                        pendingReports = pendingReportsCount,
                        draftReports = draftReports,
                        rejectedReports = rejectedReports
                    )

                    _uiState.value = _uiState.value.copy(
                        pendingReports = pendingReports,
                        statistics = statistics
                    )

                    Log.d(TAG, "Loaded ${pendingReports.size} pending reports from Firebase")
                    Log.d(TAG, "Statistics loaded: Approved=$approvedReports, Pending=$pendingReportsCount, Draft=$draftReports, Rejected=$rejectedReports")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception loading reports data from Firebase", e)
        }
    }

    private suspend fun loadTodayTasks() {
        try {
            val result = getInspectorTasksUseCase(currentUserId)
            result.fold(
                onSuccess = { tasks ->
                    val today = Calendar.getInstance()
                    val todayStart = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val todayEnd = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }

                    val todayTasks = tasks.filter { task ->
                        task.dueDate?.let { dueDate ->
                            val taskDate = dueDate.toDate().time
                            taskDate >= todayStart.timeInMillis && taskDate <= todayEnd.timeInMillis
                        } ?: false
                    }.filter { task ->
                        // Only show assigned or in-progress tasks
                        task.status == TaskStatus.ASSIGNED || task.status == TaskStatus.IN_PROGRESS
                    }.map { task ->
                        TaskItem(
                            taskId = task.taskId,
                            title = task.title.ifEmpty { "Nhiệm vụ không có tiêu đề" },
                            description = task.description.ifEmpty { "Không có mô tả" },
                            priority = task.priority.name,
                            status = task.status.name,
                            dueTime = formatTaskDueTime(task.dueDate)
                        )
                    }

                    _uiState.value = _uiState.value.copy(todayTasks = todayTasks)
                    Log.d(TAG, "Loaded ${todayTasks.size} today tasks")
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error loading today tasks", exception)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception loading today tasks", e)
        }
    }

    private fun formatTimestamp(timestamp: Timestamp?): String {
        if (timestamp == null) return "Không rõ thời gian"

        val now = System.currentTimeMillis()
        val reportTime = timestamp.toDate().time
        val diffInMillis = now - reportTime

        return when {
            diffInMillis < 60 * 1000 -> "Vừa xong"
            diffInMillis < 60 * 60 * 1000 -> "${diffInMillis / (60 * 1000)} phút trước"
            diffInMillis < 24 * 60 * 60 * 1000 -> "${diffInMillis / (60 * 60 * 1000)} giờ trước"
            diffInMillis < 7 * 24 * 60 * 60 * 1000 -> "${diffInMillis / (24 * 60 * 60 * 1000)} ngày trước"
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(timestamp.toDate())
        }
    }

    private fun formatTaskDueTime(timestamp: Timestamp?): String {
        if (timestamp == null) return "Không có hạn"

        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp.toDate())
    }

    fun refreshDashboard() {
        Log.d(TAG, "Manual refresh requested")
        loadDashboardData()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun onCreateReportClick() {
        Log.d(TAG, "Create report clicked")
        // Handle navigation to create report screen
        // This should be handled by the UI layer
    }

    // Helper methods for UI
    fun getTotalReports(): Int {
        val stats = _uiState.value.statistics
        return (stats?.approvedReports ?: 0) +
                (stats?.pendingReports ?: 0) +
                (stats?.draftReports ?: 0) +
                (stats?.rejectedReports ?: 0)
    }

    fun hasPendingWork(): Boolean {
        val stats = _uiState.value.statistics
        return (stats?.draftReports ?: 0) > 0 || (stats?.rejectedReports ?: 0) > 0
    }

    fun hasTasksToday(): Boolean {
        return _uiState.value.todayTasks.isNotEmpty()
    }

    fun hasPendingReports(): Boolean {
        return _uiState.value.pendingReports.isNotEmpty()
    }
}

data class InspectorDashboardUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val statistics: DashboardStatistics? = null,
    val pendingReports: List<ReportItem> = emptyList(),
    val todayTasks: List<TaskItem> = emptyList(),
    val errorMessage: String? = null
) {
    val showContent: Boolean get() = !isLoading && currentUser != null
    val showError: Boolean get() = errorMessage != null
    val isEmpty: Boolean get() = statistics?.let { it.getTotalCount() == 0 } ?: true
}

// Data classes for the new dashboard items
data class ReportItem(
    val id: String,
    val title: String,
    val status: String,
    val createdAt: String
)

data class TaskItem(
    val taskId: String,
    val title: String,
    val description: String,
    val priority: String,
    val status: String,
    val dueTime: String
)

data class DashboardStatistics(
    val approvedReports: Int,
    val pendingReports: Int,
    val draftReports: Int,
    val rejectedReports: Int
) {
    fun getTotalCount(): Int = approvedReports + pendingReports + draftReports + rejectedReports
}