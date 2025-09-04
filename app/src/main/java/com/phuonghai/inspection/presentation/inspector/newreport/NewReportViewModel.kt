package com.phuonghai.inspection.presentation.home.inspector.report

import android.net.Uri
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

    init {
        loadInspectorInfo()
    }

    fun loadInspectorInfo() {
        Log.d(TAG, "Loading inspector information")

        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()

                _uiState.value = _uiState.value.copy(
                    inspectorName = currentUser?.fullName ?: "Unknown Inspector",
                    inspectorId = "INSP-${currentUser?.uId?.take(6)?.uppercase() ?: "000000"}",
                    currentUserId = currentUser?.uId ?: "",
                    unreadNotifications = 4 // Mock data, replace with real count
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

    fun setTaskId(taskId: String) {
        _uiState.value = _uiState.value.copy(currentTaskId = taskId)
        Log.d(TAG, "Set taskId: $taskId")
    }

    fun loadDraftReport(reportId: String) {
        Log.d(TAG, "Loading draft report: $reportId")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                currentReportId = reportId
            )

            try {
                val result = reportRepository.getReport(reportId)
                result.fold(
                    onSuccess = { report ->
                        if (report != null) {
                            Log.d(TAG, "Draft report loaded: ${report.title}")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                currentTaskId = report.taskId,
                                message = "Draft loaded successfully"
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                message = "Draft not found"
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error loading draft", error)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "Error loading draft: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading draft", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Error loading draft: ${e.message}"
                )
            }
        }
    }

    fun submitReport(
        title: String,
        description: String,
        score: Int,
        type: InspectionType,
        status: AssignStatus,
        priority: Priority,
        address: String,
        imageUris: List<Uri>,
        videoUri: Uri?
    ) {
        Log.d(TAG, "Submitting report: $title")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                actionType = "submit"
            )

            try {
                // Upload media files first and get URLs
                val imageUrls = mutableListOf<String>()
                if (imageUris.isNotEmpty()) {
                    // Loop through the list of URIs and upload each one
                    imageUris.forEach { uri ->
                        val result = reportRepository.uploadImage(uri)
                        if (result.isSuccess) {
                            imageUrls.add(result.getOrNull() ?: "")
                        } else {
                            throw result.exceptionOrNull() ?: Exception("Unknown image upload error")
                        }
                    }
                }

                val videoUrl = videoUri?.let { uri ->
                    val result = reportRepository.uploadVideo(uri)
                    if (result.isSuccess) {
                        result.getOrNull()
                    } else {
                        throw result.exceptionOrNull() ?: Exception("Unknown video upload error")
                    }
                }

                val report = createReport(
                    title = title,
                    description = description,
                    score = score,
                    type = type,
                    assignStatus = status,
                    priority = priority,
                    address = address,
                    // Take the URL of the first image. The Report model only supports one image.
                    imageUrl = imageUrls.firstOrNull() ?: "",
                    videoUrl = videoUrl ?: "",
                    taskId = _uiState.value.currentTaskId
                )

                val result = reportRepository.createReport(report)

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionType = null,
                        message = "Report submitted successfully! ID: ${result.getOrNull()}"
                    )
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

    fun saveAsDraft(
        title: String,
        description: String,
        score: Int?,
        type: InspectionType,
        status: AssignStatus,
        priority: Priority,
        address: String,
        imageUris: List<Uri>,
        videoUri: Uri?,
        taskId: String = ""
    ) {
        Log.d(TAG, "Saving report as draft: $title for task: $taskId")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                actionType = "save"
            )

            try {
                // For drafts, we might not need to upload media immediately
                val report = createReport(
                    title = title,
                    description = description,
                    score = score,
                    type = type,
                    assignStatus = AssignStatus.DRAFT,
                    priority = priority,
                    address = address,
                    imageUrl = "", // Empty for draft
                    videoUrl = "",  // Empty for draft
                    taskId = taskId
                )

                val result = reportRepository.createReport(report)

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionType = null,
                        message = "Draft saved successfully! ID: ${result.getOrNull()}"
                    )
                    Log.d(TAG, "Draft saved successfully: ${result.getOrNull()}")
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

    private fun createReport(
        title: String,
        description: String,
        score: Int?,
        type: InspectionType,
        assignStatus: AssignStatus,
        priority: Priority,
        address: String,
        imageUrl: String,
        videoUrl: String,
        taskId: String = ""
    ): Report {
        val state = _uiState.value

        return Report(
            inspectorId = state.currentUserId,
            taskId = taskId,
            title = title,
            description = description,
            type = type,
            lat = "", // TODO: Get from location service
            lng = "", // TODO: Get from location service
            address = address,
            score = score,
            priority = priority,
            assignStatus = assignStatus,
            responseStatus = ResponseStatus.PENDING,
            syncStatus = SyncStatus.UNSYNCED,
            imageUrl = imageUrl,
            videoUrl = videoUrl,
            createdAt = Timestamp.now(),
            completedAt = if (assignStatus != AssignStatus.DRAFT) Timestamp.now() else null
        )
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    // Validation methods
    fun validateTitle(title: String): String? {
        return when {
            title.isBlank() -> "Title is required"
            title.length < 3 -> "Title must be at least 3 characters"
            title.length > 100 -> "Title must be less than 100 characters"
            else -> null
        }
    }

    fun validateDescription(description: String): String? {
        return when {
            description.isBlank() -> "Description is required"
            description.length < 10 -> "Description must be at least 10 characters"
            description.length > 1000 -> "Description must be less than 1000 characters"
            else -> null
        }
    }

    fun validateScore(scoreText: String): String? {
        return when {
            scoreText.isBlank() -> "Score is required"
            scoreText.toIntOrNull() == null -> "Score must be a valid number"
            scoreText.toInt() !in 0..100 -> "Score must be between 0 and 100"
            else -> null
        }
    }

    fun canSubmit(
        title: String,
        description: String,
        score: String
    ): Boolean {
        return validateTitle(title) == null &&
                validateDescription(description) == null &&
                validateScore(score) == null
    }

    fun canSaveDraft(
        title: String,
        description: String
    ): Boolean {
        return title.isNotBlank() || description.isNotBlank()
    }
}

data class NewReportUiState(
    val isLoading: Boolean = false,
    val inspectorName: String = "",
    val inspectorId: String = "",
    val currentUserId: String = "",
    val currentTaskId: String = "",
    val currentReportId: String = "",
    val unreadNotifications: Int = 0,
    val actionType: String? = null,
    val message: String? = null
) {
    val isSubmitting: Boolean get() = isLoading && actionType == "submit"
    val isSaving: Boolean get() = isLoading && actionType == "save"
}