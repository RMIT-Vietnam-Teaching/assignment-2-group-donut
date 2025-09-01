package com.donut.assignment2.presentation.inpsector.dashboard


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donut.assignment2.domain.model.InspectorDashboard
import com.donut.assignment2.domain.model.Report
import com.donut.assignment2.domain.model.ReportStatus
import com.donut.assignment2.domain.usecase.inspector.GetInspectorDashboardUseCase
import com.donut.assignment2.domain.usecase.inspector.GetMyReportsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InspectorDashboardViewModel @Inject constructor(
    private val getInspectorDashboardUseCase: GetInspectorDashboardUseCase,
    private val getMyReportsUseCase: GetMyReportsUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "InspectorDashboard"
    }

    private val _uiState = MutableStateFlow(InspectorDashboardUiState())
    val uiState: StateFlow<InspectorDashboardUiState> = _uiState.asStateFlow()

    fun loadDashboard(inspectorId: String) {
        Log.d(TAG, "Loading dashboard for inspector: $inspectorId")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                // Load dashboard data
                getInspectorDashboardUseCase(inspectorId)
                    .onSuccess { dashboard ->
                        Log.d(TAG, "Dashboard loaded successfully")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            dashboard = dashboard,
                            errorMessage = null
                        )

                        // Start observing reports flow
                        observeReportsFlow(inspectorId)
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Failed to load dashboard: ${error.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Không thể tải dashboard: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading dashboard", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi không xác định: ${e.message}"
                )
            }
        }
    }

    private fun observeReportsFlow(inspectorId: String) {
        Log.d(TAG, "Starting to observe reports flow")

        viewModelScope.launch {
            try {
                getMyReportsUseCase(inspectorId)
                    .catch { error ->
                        Log.e(TAG, "Error in reports flow: ${error.message}")
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Không thể tải danh sách reports: ${error.message}"
                        )
                    }
                    .collect { reports ->
                        Log.d(TAG, "Reports updated: ${reports.size} reports")
                        updateReportsData(reports)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in reports flow", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Lỗi kết nối reports: ${e.message}"
                )
            }
        }
    }

    private fun updateReportsData(reports: List<Report>) {
        val currentDashboard = _uiState.value.dashboard

        if (currentDashboard != null) {
            // Calculate fresh statistics
            val draftCount = reports.count { it.status == ReportStatus.DRAFT }
            val submittedCount = reports.count { it.status == ReportStatus.SUBMITTED || it.status == ReportStatus.UNDER_REVIEW }
            val approvedCount = reports.count { it.status == ReportStatus.APPROVED }
            val rejectedCount = reports.count { it.status == ReportStatus.REJECTED }

            // Get recent reports (latest 5)
            val recentReports = reports
                .sortedByDescending { it.updatedAt }
                .take(5)

            // Update dashboard with fresh data
            val updatedDashboard = currentDashboard.copy(
                draftReports = draftCount,
                submittedReports = submittedCount,
                approvedReports = approvedCount,
                rejectedReports = rejectedCount,
                recentReports = recentReports
            )

            _uiState.value = _uiState.value.copy(
                dashboard = updatedDashboard,
                recentReports = recentReports,
                totalReports = reports.size,
                isRefreshing = false
            )

            Log.d(TAG, "Updated statistics - Draft: $draftCount, Submitted: $submittedCount, Approved: $approvedCount, Rejected: $rejectedCount")
        }
    }

    fun refreshDashboard(inspectorId: String) {
        Log.d(TAG, "Manual refresh requested")

        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadDashboard(inspectorId)
    }

    fun clearError() {
        Log.d(TAG, "Clearing error message")
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun retryLoad(inspectorId: String) {
        Log.d(TAG, "Retry loading dashboard")
        clearError()
        loadDashboard(inspectorId)
    }

    // Helper function to get specific report counts
    fun getDraftReportsCount(): Int = _uiState.value.dashboard?.draftReports ?: 0
    fun getSubmittedReportsCount(): Int = _uiState.value.dashboard?.submittedReports ?: 0
    fun getApprovedReportsCount(): Int = _uiState.value.dashboard?.approvedReports ?: 0
    fun getRejectedReportsCount(): Int = _uiState.value.dashboard?.rejectedReports ?: 0

    // Helper function to check if user has pending work
    fun hasPendingWork(): Boolean {
        val dashboard = _uiState.value.dashboard
        return (dashboard?.draftReports ?: 0) > 0 || (dashboard?.rejectedReports ?: 0) > 0
    }

    // Helper function for UI state checks
    fun hasNoReports(): Boolean = _uiState.value.totalReports == 0
    fun isFirstTimeUser(): Boolean = _uiState.value.totalReports == 0 && !_uiState.value.isLoading
}

data class InspectorDashboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val dashboard: InspectorDashboard? = null,
    val recentReports: List<Report> = emptyList(),
    val totalReports: Int = 0,
    val errorMessage: String? = null
) {
    // Computed properties for UI convenience
    val hasData: Boolean get() = dashboard != null
    val isEmpty: Boolean get() = totalReports == 0 && !isLoading
    val showLoading: Boolean get() = isLoading && dashboard == null
    val showContent: Boolean get() = !isLoading && dashboard != null
    val showError: Boolean get() = errorMessage != null && dashboard == null
    val showRefreshing: Boolean get() = isRefreshing && dashboard != null
}