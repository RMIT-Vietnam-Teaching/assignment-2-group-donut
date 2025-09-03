package com.phuonghai.inspection.presentation.home.inspector.report

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.phuonghai.inspection.domain.model.*
import com.phuonghai.inspection.domain.repository.IAuthRepository
import com.phuonghai.inspection.domain.repository.IReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewReportViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val reportRepository: IReportRepository
) : ViewModel() {

    companion object {
        private const val TAG = "NewReportVM"
    }

    private val _uiState = MutableStateFlow(NewReportUiState())
    val uiState: StateFlow<NewReportUiState> = _uiState.asStateFlow()

    fun loadInspectorInfo() {
        Log.d(TAG, "Loading inspector information")

        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()

                _uiState.value = _uiState.value.copy(
                    inspectorName = currentUser?.fullName ?: "Unknown Inspector",
                    inspectorId = "INSP-${currentUser?.uId?.take(5) ?: "00000"}",
                    currentUserId = currentUser?.uId ?: "",
                    unreadNotifications = 4
                )

                Log.d(TAG, "Inspector info loaded: ${currentUser?.fullName}")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading inspector info", e)
                _uiState.value = _uiState.value.copy(
                    message = "Error loading inspector info: ${e.message}"
                )
            }
        }
    }

    // Các update methods
    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun updateScore(score: String) {
        _uiState.value = _uiState.value.copy(score = score)
    }

    fun updateOutcome(outcome: String) {
        _uiState.value = _uiState.value.copy(outcome = outcome)
    }

    fun updateStatus(status: String) {
        _uiState.value = _uiState.value.copy(status = status)
    }

    fun updateInspectionType(type: String) {
        _uiState.value = _uiState.value.copy(inspectionType = type)
    }

    // ✅ Method chính để tạo report
    fun submitReport() {
        Log.d(TAG, "Submitting report")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                actionType = "submit"
            )

            try {
                val assignStatus = when (_uiState.value.status) {
                    "Passed" -> AssignStatus.PASSED
                    "Failed" -> AssignStatus.FAILED
                    "Needs Attention" -> AssignStatus.NEEDS_ATTENTION
                    else -> AssignStatus.PENDING_REVIEW
                }

                val report = createReportFromUiState(assignStatus)
                val result = reportRepository.createReport(report)

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionType = null,
                        message = "Report submitted successfully"
                    )
                    clearForm()
                    Log.d(TAG, "Report submitted successfully: ${result.getOrNull()}")
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionType = null,
                        message = "Failed to submit report: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error submitting report", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    actionType = null,
                    message = "Error submitting report: ${e.message}"
                )
            }
        }
    }

    fun saveAsDraft() {
        Log.d(TAG, "Saving report as draft")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                actionType = "save"
            )

            try {
                val report = createReportFromUiState(AssignStatus.DRAFT)
                val result = reportRepository.createReport(report)

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionType = null,
                        message = "Report saved as draft successfully"
                    )
                    clearForm()
                    Log.d(TAG, "Report saved as draft successfully: ${result.getOrNull()}")
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionType = null,
                        message = "Failed to save draft: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving draft", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    actionType = null,
                    message = "Error saving draft: ${e.message}"
                )
            }
        }
    }

    // ✅ Method để tạo Report object từ UI state
    private fun createReportFromUiState(assignStatus: AssignStatus): Report {
        val state = _uiState.value

        return Report(
            inspectorId = state.currentUserId,
            title = state.title,
            description = state.notes,
            type = when (state.inspectionType.uppercase()) {
                "FIRE SAFETY" -> InspectionType.FIRE_SAFETY
                "FOOD HYGIENE" -> InspectionType.FOOD_HYGIENE
                else -> InspectionType.valueOf(state.inspectionType.uppercase())
            },
            score = state.score.toIntOrNull(),
            priority = Priority.NORMAL,
            assignStatus = assignStatus,
            responseStatus = ResponseStatus.PENDING,
            syncStatus = SyncStatus.UNSYNCED,
            createdAt = Timestamp.now(),
            completedAt = if (assignStatus != AssignStatus.DRAFT) Timestamp.now() else null
        )
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    private fun clearForm() {
        _uiState.value = _uiState.value.copy(
            title = "",
            notes = "",
            score = "",
            outcome = "",
            status = "Passed",
            inspectionType = "Electrical"
        )
    }

    fun validateForm(): Boolean {
        val state = _uiState.value
        return state.title.isNotBlank() &&
                state.notes.isNotBlank() &&
                state.score.isNotBlank()
    }
}

data class NewReportUiState(
    val isLoading: Boolean = false,
    val inspectorName: String = "",
    val inspectorId: String = "",
    val currentUserId: String = "",
    val unreadNotifications: Int = 0,
    val title: String = "",
    val notes: String = "",
    val score: String = "",
    val outcome: String = "",
    val status: String = "Passed",
    val inspectionType: String = "Electrical",
    val actionType: String? = null,
    val message: String? = null
) {
    val canSubmit: Boolean get() = title.isNotBlank() &&
            notes.isNotBlank() &&
            score.isNotBlank()
}