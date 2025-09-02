package com.phuonghai.inspection.presentation.home.inspector.report

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phuonghai.inspection.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewReportViewModel @Inject constructor(
    private val authRepository: IAuthRepository
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
                    inspectorId = "INSP-${currentUser?.uid?.take(5) ?: "00000"}",
                    unreadNotifications = 4 // Sample data
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

    fun saveAsDraft() {
        Log.d(TAG, "Saving report as draft")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                actionType = "save"
            )

            try {
                // TODO: Implement save as draft logic
                kotlinx.coroutines.delay(1500) // Simulate API call

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    actionType = null,
                    message = "Report saved as draft successfully"
                )

                Log.d(TAG, "Report saved as draft successfully")
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

    fun submitReport() {
        Log.d(TAG, "Submitting report")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                actionType = "submit"
            )

            try {
                // TODO: Implement submit report logic
                kotlinx.coroutines.delay(2000) // Simulate API call

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    actionType = null,
                    message = "Report submitted successfully"
                )

                // Clear form after successful submission
                clearForm()

                Log.d(TAG, "Report submitted successfully")
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
    val unreadNotifications: Int = 0,
    val title: String = "",
    val notes: String = "",
    val score: String = "",
    val outcome: String = "",
    val status: String = "Passed",
    val inspectionType: String = "Electrical",
    val actionType: String? = null, // "save" or "submit"
    val message: String? = null
) {
    val canSubmit: Boolean get() = title.isNotBlank() && notes.isNotBlank() && score.isNotBlank()
}