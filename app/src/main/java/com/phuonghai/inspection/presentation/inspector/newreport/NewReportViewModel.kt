package com.phuonghai.inspection.presentation.home.inspector.report

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.phuonghai.inspection.data.repository.OfflineReportRepository
import com.phuonghai.inspection.domain.common.Priority
import com.phuonghai.inspection.domain.model.*
import com.phuonghai.inspection.domain.repository.IAuthRepository
import com.phuonghai.inspection.domain.repository.IReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

// Data class để quản lý toàn bộ trạng thái của màn hình
data class NewReportUiState(
    val isLoading: Boolean = false,
    val actionType: String? = null,
    val message: String? = null,
    val inspectorName: String = "",
    val inspectorId: String = "",
    val currentUserId: String = "",
    val currentTaskId: String = "",
    val currentReportId: String = "",
    val unreadNotifications: Int = 0,
    val shouldNavigateBack: Boolean = false,
    // Trạng thái của các trường trong form
    val title: String = "",
    val description: String = "",
    val score: String = "",
    val address: String = "",
    val type: InspectionType = InspectionType.ELECTRICAL,
    val assignStatus: AssignStatus = AssignStatus.PASSED,
    val priority: Priority = Priority.NORMAL
) {
    val isSubmitting: Boolean get() = isLoading && actionType == "submit"
    val isSaving: Boolean get() = isLoading && actionType == "save"
}


@HiltViewModel
class NewReportViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val reportRepository: IReportRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "NewReportVM"
    }

    private val _uiState = MutableStateFlow(NewReportUiState())
    val uiState: StateFlow<NewReportUiState> = _uiState.asStateFlow()

    init {
        loadInspectorInfo()
    }

    // CÁC HÀM CẬP NHẬT TRẠNG THÁI FORM TỪ UI
    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }
    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }
    fun onAddressChange(newAddress: String) {
        _uiState.update { it.copy(address = newAddress) }
    }
    fun onScoreChange(newScore: String) {
        if (newScore.isEmpty() || (newScore.matches(Regex("^\\d+\$")) && newScore.toInt() in 0..100)) {
            _uiState.update { it.copy(score = newScore) }
        }
    }
    fun onTypeChange(newType: InspectionType) {
        _uiState.update { it.copy(type = newType) }
    }
    fun onStatusChange(newStatus: AssignStatus) {
        _uiState.update { it.copy(assignStatus = newStatus) }
    }
    fun onPriorityChange(newPriority: Priority) {
        _uiState.update { it.copy(priority = newPriority) }
    }

    private fun loadInspectorInfo() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                _uiState.update {
                    it.copy(
                        inspectorName = currentUser?.fullName ?: "Unknown Inspector",
                        inspectorId = "INSP-${currentUser?.uId?.take(6)?.uppercase() ?: "000000"}",
                        currentUserId = currentUser?.uId ?: ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Error loading inspector info: ${e.message}") }
            }
        }
    }

    fun setTaskId(taskId: String) {
        _uiState.update { it.copy(currentTaskId = taskId) }
    }

    fun loadDraftByTaskId(taskId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, currentTaskId = taskId) }
            reportRepository.getDraftReportByTaskId(taskId).fold(
                onSuccess = { report ->
                    if (report != null) {
                        populateStateFromReport(report, "Draft loaded from Task ID")
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, message = "Error loading draft: ${error.message}") }
                }
            )
        }
    }

    fun loadDraftReport(reportId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, currentReportId = reportId) }
            reportRepository.getReport(reportId).fold(
                onSuccess = { report ->
                    if (report != null) {
                        populateStateFromReport(report, "Draft loaded from Report ID")
                    } else {
                        _uiState.update { it.copy(isLoading = false, message = "Draft not found") }
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, message = "Error loading draft: ${error.message}") }
                }
            )
        }
    }

    private fun populateStateFromReport(report: Report, message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                currentReportId = report.reportId,
                currentTaskId = report.taskId,
                title = report.title,
                description = report.description,
                score = report.score?.toString() ?: "",
                address = report.address,
                type = report.type,
                assignStatus = report.assignStatus,
                priority = report.priority,
                message = message
            )
        }
    }

    fun generateDescriptionFromImage(imageUri: Uri) {
        viewModelScope.launch {
            try {
                val inputImage = InputImage.fromFilePath(context, imageUri)
                val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

                labeler.process(inputImage)
                    .addOnSuccessListener { labels ->
                      val MIN_CONFIDENCE = 0.65f // Đặt ngưỡng tin cậy là 65%

                        // 1. Định nghĩa các từ khóa tình trạng (mở rộng hơn)
                        val conditionKeywords = setOf(
                            "rust", "crack", "leak", "broken", "damage", "fire", "smoke",
                            "dent", "scratch", "wear", "tear", "stain", "corrosion", "fracture"
                        )

                        // 2. Lọc các nhãn có độ tin cậy cao VÀ phân loại chúng
                        val objects = mutableListOf<String>()
                        val conditions = mutableListOf<String>()

                        labels.filter { it.confidence >= MIN_CONFIDENCE } // <-- LỌC THEO ĐỘ TIN CẬY
                            .take(5) // Lấy 5 nhãn TỐT NHẤT
                            .forEach { label ->
                                if (label.text.lowercase(Locale.ROOT) in conditionKeywords) {
                                    conditions.add(label.text)
                                } else {
                                    objects.add(label.text)
                                }
                            }

                        // 3. Tạo mô tả thông minh hơn
                        var generatedText = ""
                        if (objects.isNotEmpty()) {
                            generatedText += "Objects identified: ${objects.joinToString(", ")}. "
                        }
                        if (conditions.isNotEmpty()) {
                            generatedText += "Possible conditions: ${conditions.joinToString(", ")}."
                        }

                        // Chỉ cập nhật nếu có kết quả hợp lệ
                        if (generatedText.isNotBlank()) {
                            val currentDescription = _uiState.value.description
                            val newDescription = if (currentDescription.isNotBlank()) {
                                "$currentDescription\n\n$generatedText"
                            } else {
                                generatedText
                            }
                            _uiState.update { it.copy(description = newDescription) }
                        }
                   }
                    .addOnFailureListener { e -> Log.e(TAG, "Image labeling failed", e) }

            } catch (e: Exception) {
                Log.e(TAG, "Error preparing image for ML Kit", e)
            }
        }
    }

    fun saveAsDraft(imageUris: List<Uri>, videoUri: Uri?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, actionType = "save") }
            val report = createReportFromState().copy(assignStatus = AssignStatus.DRAFT)
            handleSaveOrUpdate(report, imageUris, videoUri, isSubmit = false)
        }
    }

    fun submitReport(imageUris: List<Uri>, videoUri: Uri?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, actionType = "submit") }
            val report = createReportFromState()
            handleSaveOrUpdate(report, imageUris, videoUri, isSubmit = true)
        }
    }

    private suspend fun handleSaveOrUpdate(report: Report, imageUris: List<Uri>, videoUri: Uri?, isSubmit: Boolean) {
        val repo = reportRepository as? OfflineReportRepository
            ?: run {
                _uiState.update { it.copy(isLoading = false, message = "Error: Invalid repository type") }
                return
            }

        val result = repo.createReportWithMedia(report, imageUris, videoUri)
        result.fold(
            onSuccess = {
                if (isSubmit) {
                    deleteDraftAfterSubmit(report.taskId)
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        actionType = null,
                        message = if (isSubmit) "Report submitted successfully" else "Draft saved successfully",
                        shouldNavigateBack = true
                    )
                }
            },
            onFailure = { error ->
                _uiState.update { it.copy(isLoading = false, actionType = null, message = "Operation failed: ${error.message}") }
            }
        )
    }

    private suspend fun deleteDraftAfterSubmit(taskId: String) {
        if (taskId.isBlank()) return
        reportRepository.getDraftReportByTaskId(taskId).getOrNull()?.let { draft ->
            reportRepository.deleteDraftReport(draft.reportId)
        }
    }

    private fun createReportFromState(): Report {
        val state = _uiState.value
        return Report(
            reportId = state.currentReportId.ifEmpty { "" },
            inspectorId = state.currentUserId,
            taskId = state.currentTaskId,
            title = state.title,
            description = state.description,
            type = state.type,
            address = state.address,
            score = state.score.toIntOrNull(),
            priority = state.priority,
            assignStatus = state.assignStatus,
            responseStatus = ResponseStatus.PENDING,
            syncStatus = SyncStatus.UNSYNCED,
            createdAt = Timestamp.now(),
            completedAt = if (state.assignStatus != AssignStatus.DRAFT) Timestamp.now() else null
        )
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun clearNavigationFlag() {
        _uiState.update { it.copy(shouldNavigateBack = false) }
    }
}