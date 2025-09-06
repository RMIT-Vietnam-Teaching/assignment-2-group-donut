package com.phuonghai.inspection.presentation.supervisor.report

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phuonghai.inspection.domain.model.Report
import com.phuonghai.inspection.domain.model.ResponseStatus
import com.phuonghai.inspection.domain.model.TaskStatus
import com.phuonghai.inspection.domain.usecase.GetReportUseCase
import com.phuonghai.inspection.domain.usecase.GetTaskIdByReportIdUseCase
import com.phuonghai.inspection.domain.usecase.UpdateResponseStatusReportUseCase
import com.phuonghai.inspection.domain.usecase.UpdateTaskStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupervisorReportDetailViewModel @Inject constructor(
    private val getReportUseCase: GetReportUseCase,
    private val updateResponseStatusReportUseCase: UpdateResponseStatusReportUseCase,
    private val getTaskIdByReportIdUseCase: GetTaskIdByReportIdUseCase,
    private val updateTaskStatusUseCase: UpdateTaskStatusUseCase
) : ViewModel() {

    private val _report = MutableStateFlow<Report?>(null)
    val report = _report.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadReport(reportId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            getReportUseCase(reportId)
                .onSuccess { reportData ->
                    _report.value = reportData
                }
                .onFailure { e ->
                    _error.value = e.message ?: "Unknown error"
                }
            _isLoading.value = false
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

                // 4. Reload data if everything succeeded
                loadReport(reportId)
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("SupervisorDashboardViewModel", "Error approving report", e)
            }
        }
    }
    fun rejectReport(reportId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = updateResponseStatusReportUseCase(reportId, ResponseStatus.REJECTED.name)

            result.onSuccess {
                loadReport(reportId)
                _isLoading.value = false
            }.onFailure { e ->
                Log.e("SupervisorDashboardViewModel", "Error rejecting report", e)
            }
        }
    }
}