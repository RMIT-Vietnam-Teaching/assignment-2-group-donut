package com.phuonghai.inspection.presentation.home.inspector

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
class InspectorDashboardViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "InspectorDashboardVM"
    }

    private val _uiState = MutableStateFlow(InspectorDashboardUiState())
    val uiState: StateFlow<InspectorDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        Log.d(TAG, "Loading dashboard data")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Get current user info
                val currentUser = authRepository.getCurrentUser()
                Log.d(TAG, "Current user: $currentUser")

                if (currentUser != null) {
                    // Load dashboard statistics
                    loadStatistics(currentUser)

                    // Load recent reports (mock data for now)
                    loadRecentReports()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = currentUser,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Không thể tải thông tin người dùng"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading dashboard", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi tải dashboard: ${e.message}"
                )
            }
        }
    }

    private fun loadStatistics(user: User) {
        // Mock data - replace with actual repository calls
        val stats = DashboardStatistics(
            approvedReports = 12,
            pendingReports = 3,
            draftReports = 5,
            rejectedReports = 1
        )

        _uiState.value = _uiState.value.copy(statistics = stats)
        Log.d(TAG, "Loaded statistics: $stats")
    }

    private fun loadRecentReports() {
        // Mock data - replace with actual repository calls
        val recentReports = listOf(
            ReportItem(
                id = "1",
                title = "Kiểm tra an toàn công trình A",
                status = "SUBMITTED",
                createdAt = "2 giờ trước"
            ),
            ReportItem(
                id = "2",
                title = "Báo cáo tình trạng máy móc",
                status = "APPROVED",
                createdAt = "1 ngày trước"
            ),
            ReportItem(
                id = "3",
                title = "Kiểm tra môi trường làm việc",
                status = "DRAFT",
                createdAt = "2 ngày trước"
            )
        )

        _uiState.value = _uiState.value.copy(recentReports = recentReports)
        Log.d(TAG, "Loaded ${recentReports.size} recent reports")
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
}

data class InspectorDashboardUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val statistics: DashboardStatistics? = null,
    val recentReports: List<ReportItem> = emptyList(),
    val errorMessage: String? = null
) {
    val showContent: Boolean get() = !isLoading && currentUser != null
    val showError: Boolean get() = errorMessage != null
    val isEmpty: Boolean get() = statistics?.let { it.getTotalCount() == 0 } ?: true
}

data class DashboardStatistics(
    val approvedReports: Int = 0,
    val pendingReports: Int = 0,
    val draftReports: Int = 0,
    val rejectedReports: Int = 0
) {
    fun getTotalCount(): Int = approvedReports + pendingReports + draftReports + rejectedReports

    fun getStatusSummary(): String {
        return when {
            pendingReports > 0 && draftReports > 0 ->
                "Bạn có $pendingReports báo cáo đang chờ duyệt và $draftReports bản nháp"
            pendingReports > 0 ->
                "Bạn có $pendingReports báo cáo đang chờ duyệt"
            draftReports > 0 ->
                "Bạn có $draftReports bản nháp cần hoàn thiện"
            getTotalCount() == 0 ->
                "Chưa có báo cáo nào. Hãy tạo báo cáo đầu tiên!"
            else ->
                "Tất cả báo cáo đã được xử lý"
        }
    }
}

data class ReportItem(
    val id: String,
    val title: String,
    val status: String,
    val createdAt: String
)