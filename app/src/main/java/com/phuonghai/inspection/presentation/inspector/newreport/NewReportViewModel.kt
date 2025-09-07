package com.phuonghai.inspection.presentation.home.inspector.report

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.phuonghai.inspection.domain.common.Priority
import com.phuonghai.inspection.domain.model.*
import com.phuonghai.inspection.domain.repository.IAuthRepository
import com.phuonghai.inspection.domain.repository.IReportRepository
import com.phuonghai.inspection.data.repository.OfflineReportRepository
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
                            Log.d(TAG, "Draft report loaded successfully: ${report.title}")
                            Log.d(TAG, "Report data: $report")

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                currentTaskId = report.taskId,
                                draftData = report,
                                message = "Draft loaded successfully"
                            )
                        } else {
                            Log.w(TAG, "Draft report not found for ID: $reportId")
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

    fun loadDraftByTaskId(taskId: String) {
        Log.d(TAG, "Loading latest draft report by taskId: $taskId")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val result = reportRepository.getDraftReportByTaskId(taskId)
                result.fold(
                    onSuccess = { report ->
                        if (report != null) {
                            Log.d(TAG, "Latest draft found for taskId $taskId: reportId=${report.reportId}, created=${report.createdAt}")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                currentReportId = report.reportId,
                                currentTaskId = taskId,
                                draftData = report,
                                message = "Latest draft loaded successfully"
                            )
                        } else {
                            Log.d(TAG, "No draft found for taskId: $taskId")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                currentTaskId = taskId,
                                currentReportId = "",
                                draftData = null
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error loading latest draft by taskId", error)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentTaskId = taskId,
                            currentReportId = "",
                            message = "Error loading draft: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading latest draft by taskId", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentTaskId = taskId,
                    currentReportId = "",
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
        Log.d(TAG, "Submitting report: $title with status: $status")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                actionType = "submit"
            )

            try {
                val existingReportId = _uiState.value.currentReportId
                val taskId = _uiState.value.currentTaskId

                val report = createReport(
                    title = title,
                    description = description,
                    score = score,
                    type = type,
                    assignStatus = status,
                    priority = priority,
                    address = address,
                    imageUrl = "", // Will be set by repository
                    videoUrl = "", // Will be set by repository
                    taskId = taskId
                )

                // ✅ Use the new offline-capable method
                val result = if (existingReportId.isNotEmpty()) {
                    Log.d(TAG, "Converting existing draft to submitted report: $existingReportId")
                    val updatedReport = report.copy(reportId = existingReportId)

                    // For offline repository, we need to handle media differently
                    if (reportRepository is OfflineReportRepository) {
                        reportRepository.createReportWithMedia(updatedReport, imageUris, videoUri)
                    } else {
                        // Fallback to old method for online-only
                        uploadMediaAndUpdateReport(updatedReport, imageUris, videoUri)
                    }
                } else {
                    Log.d(TAG, "Creating new report with status: $status")

                    if (reportRepository is OfflineReportRepository) {
                        reportRepository.createReportWithMedia(report, imageUris, videoUri)
                    } else {
                        // Fallback to old method for online-only
                        uploadMediaAndCreateReport(report, imageUris, videoUri)
                    }
                }

                if (result.isSuccess) {
                    val reportId = result.getOrNull()?.toString() ?: existingReportId

                    // ✅ Delete draft after successful submit
                    deleteDraftAfterSubmit(taskId, existingReportId)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionType = null,
                        message = "Report submitted successfully! ID: $reportId",
                        shouldNavigateBack = true,
                        currentReportId = "", // Clear since submitted
                        draftData = null // Clear draft data
                    )
                    Log.d(TAG, "Report submitted successfully: $reportId with status: $status")
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

    // ✅ Helper method for backward compatibility with online-only repository
    private suspend fun uploadMediaAndCreateReport(
        report: Report,
        imageUris: List<Uri>,
        videoUri: Uri?
    ): Result<String> {
        val imageUrls = mutableListOf<String>()
        if (imageUris.isNotEmpty()) {
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

        val reportWithMedia = report.copy(
            imageUrl = imageUrls.firstOrNull() ?: "",
            videoUrl = videoUrl ?: ""
        )

        return reportRepository.createReport(reportWithMedia)
    }

    // ✅ Helper method for backward compatibility with online-only repository
    private suspend fun uploadMediaAndUpdateReport(
        report: Report,
        imageUris: List<Uri>,
        videoUri: Uri?
    ): Result<String> {
        val imageUrls = mutableListOf<String>()
        if (imageUris.isNotEmpty()) {
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

        val reportWithMedia = report.copy(
            imageUrl = imageUrls.firstOrNull() ?: "",
            videoUrl = videoUrl ?: ""
        )

        val updateResult = reportRepository.updateReport(reportWithMedia)
        return if (updateResult.isSuccess) {
            Result.success(reportWithMedia.reportId)
        } else {
            updateResult.map { reportWithMedia.reportId }
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
                val existingReportId = _uiState.value.currentReportId
                val finalTaskId = taskId.ifEmpty { _uiState.value.currentTaskId }

                Log.d(TAG, "Existing reportId: $existingReportId, taskId: $finalTaskId")

                val report = createReport(
                    title = title,
                    description = description,
                    score = score,
                    type = type,
                    assignStatus = AssignStatus.DRAFT, // ✅ Always DRAFT for save as draft
                    priority = priority,
                    address = address,
                    imageUrl = "", // Will be handled by repository
                    videoUrl = "", // Will be handled by repository
                    taskId = finalTaskId
                )

                val result = if (existingReportId.isNotEmpty()) {
                    Log.d(TAG, "Updating existing draft: $existingReportId")
                    val updatedReport = report.copy(reportId = existingReportId)

                    if (reportRepository is OfflineReportRepository) {
                        reportRepository.createReportWithMedia(updatedReport, imageUris, videoUri)
                    } else {
                        // Fallback for online-only repository
                        uploadMediaAndUpdateReport(updatedReport, imageUris, videoUri)
                    }
                } else {
                    Log.d(TAG, "Creating new draft")

                    if (reportRepository is OfflineReportRepository) {
                        reportRepository.createReportWithMedia(report, imageUris, videoUri)
                    } else {
                        // Fallback for online-only repository
                        uploadMediaAndCreateReport(report, imageUris, videoUri)
                    }
                }

                if (result.isSuccess) {
                    val savedReportId = result.getOrNull()?.toString() ?: existingReportId

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionType = null,
                        currentReportId = savedReportId,
                        message = "Draft saved successfully! ID: $savedReportId",
                        shouldNavigateBack = true // ✅ Navigate back after save
                    )
                    Log.d(TAG, "Draft saved successfully: $savedReportId")
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

    private suspend fun deleteDraftAfterSubmit(taskId: String, currentReportId: String) {
        try {
            if (currentReportId.isNotEmpty()) {
                // Nếu đang edit draft thì không cần xóa vì đã convert thành submitted report
                Log.d(TAG, "Draft $currentReportId was converted to submitted report, no deletion needed")
                return
            }

            // Tìm và xóa draft của task này (nếu có)
            val existingDraft = reportRepository.getDraftReportByTaskId(taskId).getOrNull()
            if (existingDraft != null) {
                Log.d(TAG, "Deleting draft ${existingDraft.reportId} after successful submit")
                val deleteResult = reportRepository.deleteDraftReport(existingDraft.reportId)

                if (deleteResult.isSuccess) {
                    Log.d(TAG, "Draft deleted successfully after submit")
                } else {
                    Log.w(TAG, "Failed to delete draft after submit: ${deleteResult.exceptionOrNull()?.message}")
                }
            } else {
                Log.d(TAG, "No draft found for task $taskId, nothing to delete")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Exception while deleting draft after submit", e)
            // Không throw error vì submit đã thành công
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

    fun deleteDraftAndCreateNew(taskId: String) {
        viewModelScope.launch {
            try {
                // Tìm và xóa draft cũ nếu có
                val existingDraft = reportRepository.getDraftReportByTaskId(taskId).getOrNull()
                if (existingDraft != null) {
                    Log.d(TAG, "Deleting old draft: ${existingDraft.reportId}")
                    reportRepository.deleteDraftReport(existingDraft.reportId)
                }

                // Reset state để tạo draft mới
                _uiState.value = _uiState.value.copy(
                    currentReportId = "",
                    currentTaskId = taskId,
                    draftData = null
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting old draft", e)
            }
        }
    }

    fun clearNavigationFlag() {
        _uiState.value = _uiState.value.copy(shouldNavigateBack = false)
    }
}

data class NewReportUiState(
    val isLoading: Boolean = false,
    val actionType: String? = null,
    val message: String? = null,
    val inspectorName: String = "",
    val inspectorId: String = "",
    val currentUserId: String = "",
    val currentTaskId: String = "",
    val currentReportId: String = "",
    val draftData: Report? = null,
    val unreadNotifications: Int = 0,
    val shouldNavigateBack: Boolean = false
) {
    val isSubmitting: Boolean get() = isLoading && actionType == "submit"
    val isSaving: Boolean get() = isLoading && actionType == "save"
}