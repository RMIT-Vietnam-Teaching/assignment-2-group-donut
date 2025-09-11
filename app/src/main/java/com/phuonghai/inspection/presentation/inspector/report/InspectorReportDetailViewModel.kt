package com.phuonghai.inspection.presentation.inspector.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phuonghai.inspection.domain.model.Report
import com.phuonghai.inspection.domain.repository.IReportRepository
import com.phuonghai.inspection.domain.repository.ITaskRepository
import com.phuonghai.inspection.domain.repository.IUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data class mới để chứa tất cả thông tin cần thiết cho UI
data class ReportDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val reportTitle: String = "",
    val inspectorName: String = "",
    val taskTitle: String = "",
    val imageUrl: String = "",
    val videoUrl: String = "",
    val fullReport: Report? = null // Giữ lại report gốc để hiển thị các thông tin khác
)

@HiltViewModel
class InspectorReportDetailViewModel @Inject constructor(
    private val reportRepository: IReportRepository,
    private val userRepository: IUserRepository, // Thêm UserRepository
    private val taskRepository: ITaskRepository, // Thêm TaskRepository
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState: StateFlow<ReportDetailUiState> = _uiState.asStateFlow()

    init {
        val reportId = savedStateHandle.get<String>("reportId")
        if (reportId != null) {
            loadReportDetails(reportId)
        } else {
            _uiState.value = ReportDetailUiState(isLoading = false, error = "Report ID not found.")
        }
    }

    private fun loadReportDetails(reportId: String) {
        viewModelScope.launch {
            _uiState.value = ReportDetailUiState(isLoading = true) // Reset state
            val reportResult = reportRepository.getReport(reportId)

            reportResult.fold(
                onSuccess = { report ->
                    if (report != null) {
                        // Lấy tên inspector và tiêu đề task song song để tăng tốc độ
                        val inspectorDeferred = async { userRepository.getUserById(report.inspectorId) }
                        val taskDeferred = async { taskRepository.getTask(report.taskId) }

                        val inspectorResult = inspectorDeferred.await()
                        val taskResult = taskDeferred.await()

                        val inspectorName = inspectorResult.getOrNull()?.fullName ?: "Unknown Inspector"
                        val taskTitle = taskResult.getOrNull()?.title ?: "Unknown Task"

                        _uiState.value = ReportDetailUiState(
                            isLoading = false,
                            reportTitle = report.title,
                            inspectorName = inspectorName,
                            taskTitle = taskTitle,
                            imageUrl = report.imageUrl,
                            videoUrl = report.videoUrl,
                            fullReport = report
                        )
                    } else {
                        _uiState.value = ReportDetailUiState(isLoading = false, error = "Report not found.")
                    }
                },
                onFailure = { exception ->
                    _uiState.value = ReportDetailUiState(isLoading = false, error = "Failed to load report: ${exception.message}")
                }
            )
        }
    }
}