package com.phuonghai.inspection.presentation.supervisor.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phuonghai.inspection.domain.model.User
import com.phuonghai.inspection.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupervisorDashboardViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SupervisorDashboardVM"
    }

    private val _uiState = MutableStateFlow(SupervisorDashboardUiState())
    val uiState: StateFlow<SupervisorDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        Log.d(TAG, "Loading supervisor dashboard data")
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = authRepository.getCurrentUser()
                Log.d(TAG, "Current supervisor: $currentUser")
                
                if (currentUser != null) {
                    loadTeamStatistics(currentUser)
                    loadPendingReviews()
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = currentUser,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Không thể tải thông tin supervisor"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading supervisor dashboard", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi tải dashboard: ${e.message}"
                )
            }
        }
    }

    private fun loadTeamStatistics(user: User) {
        // Mock data - replace with actual repository calls
        val stats = TeamStatistics(
            pendingReviews = 5,
            approvedReports = 28,
            rejectedReports = 2,
            totalReports = 35
        )
        
        _uiState.value = _uiState.value.copy(teamStats = stats)
        Log.d(TAG, "Loaded team statistics: $stats")
    }

    private fun loadPendingReviews() {
        // Mock data - replace with actual repository calls
        val pendingReviews = listOf(
            PendingReport(
                id = "1",
                title = "Kiểm tra an toàn - Công trình B",
                inspectorName = "Inspector A",
                submittedAt = "2 giờ trước"
            ),
            PendingReport(
                id = "2",
                title = "Báo cáo môi trường",
                inspectorName = "Inspector B",
                submittedAt = "5 giờ trước"
            ),
            PendingReport(
                id = "3",
                title = "Kiểm tra thiết bị",
                inspectorName = "Inspector C",
                submittedAt = "1 ngày trước"
            )
        )
        
        _uiState.value = _uiState.value.copy(pendingReviews = pendingReviews)
        Log.d(TAG, "Loaded ${pendingReviews.size} pending reviews")
    }

    fun refreshDashboard() {
        Log.d(TAG, "Manual refresh requested")
        loadDashboardData()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun approveReport(reportId: String) {
        Log.d(TAG, "Approving report: $reportId")
        // Handle approve action
    }

    fun rejectReport(reportId: String) {
        Log.d(TAG, "Rejecting report: $reportId")
        // Handle reject action
    }

    fun viewAllReports() {
        Log.d(TAG, "View all reports clicked")
        // Handle navigation
    }

    fun viewAnalytics() {
        Log.d(TAG, "View analytics clicked")
        // Handle navigation
    }
}

data class SupervisorDashboardUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val teamStats: TeamStatistics? = null,
    val pendingReviews: List<PendingReport> = emptyList(),
    val errorMessage: String? = null
) {
    val showContent: Boolean get() = !isLoading && currentUser != null
    val showError: Boolean get() = errorMessage != null
    val hasPendingWork: Boolean get() = (teamStats?.pendingReviews ?: 0) > 0
}

data class TeamStatistics(
    val pendingReviews: Int = 0,
    val approvedReports: Int = 0,
    val rejectedReports: Int = 0,
    val totalReports: Int = 0
) {
    fun getStatusSummary(): String {
        return when {
            pendingReviews > 0 -> "Hôm nay có $pendingReviews báo cáo cần duyệt từ team"
            totalReports == 0 -> "Chưa có báo cáo nào từ team"
            else -> "Tất cả báo cáo đã được xử lý"
        }
    }
}

data class PendingReport(
    val id: String,
    val title: String,
    val inspectorName: String,
    val submittedAt: String
)