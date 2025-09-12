package com.phuonghai.inspection.presentation.inspector.task

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phuonghai.inspection.core.network.NetworkMonitor
import com.phuonghai.inspection.domain.model.Report
import com.phuonghai.inspection.domain.repository.IAuthRepository
import com.phuonghai.inspection.domain.repository.ITaskRepository
import com.phuonghai.inspection.domain.model.Task
import com.phuonghai.inspection.domain.model.TaskStatus
import com.phuonghai.inspection.domain.common.Priority
import com.phuonghai.inspection.domain.repository.IReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InspectorTaskViewModel @Inject constructor(
    private val taskRepository: ITaskRepository,
    private val reportRepository: IReportRepository, // <-- THÊM VÀO
    private val authRepository: IAuthRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    companion object {
        private const val TAG = "InspectorTaskViewModel"
    }

    private val _uiState = MutableStateFlow(InspectorTaskUiState())
    val uiState: StateFlow<InspectorTaskUiState> = _uiState.asStateFlow()

    private val _filteredTasks = MutableStateFlow<List<TaskWithDetails>>(emptyList())
    val filteredTasks: StateFlow<List<TaskWithDetails>> = _filteredTasks.asStateFlow()

    private val _syncState = MutableStateFlow<TaskSyncUiState>(TaskSyncUiState.Idle)
    val syncState: StateFlow<TaskSyncUiState> = _syncState.asStateFlow()

    private val _networkState = MutableStateFlow(false)
    val networkState: StateFlow<Boolean> = _networkState.asStateFlow()

    // Store all tasks for filtering
    private var allTasks = listOf<TaskWithDetails>()

    init {
        loadTasks()
        observeSyncProgress()
        observeNetworkState()
        getOfflineTasksInfo()
    }

    fun loadTasks() {
        Log.d(TAG, "Loading inspector tasks")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, showError = false)

            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showError = true,
                        errorMessage = "Không thể xác định inspector hiện tại"
                    )
                    return@launch
                }

                val isConnected = networkMonitor.isConnected.first()
                val localTasks = getLocalTasks(currentUser.uId)

                if (localTasks.isNotEmpty()) {
                    updateTasksDisplay(localTasks)
                    _uiState.value = _uiState.value.copy(isLoading = false, showError = false)
                }

                if (isConnected) {
                    syncOnlineTasks(currentUser.uId)
                } else {
                    if (localTasks.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            showError = true,
                            errorMessage = "Không có task nào được cache. Vui lòng kết nối internet để đồng bộ."
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading tasks", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showError = true,
                    errorMessage = "Lỗi tải task: ${e.message}"
                )
            }
        }
    }

    private suspend fun getLocalTasks(inspectorId: String): List<Task> {
        return try {
            val result = taskRepository.getTasksByInspectorId(inspectorId)
            result.getOrElse {
                Log.w(TAG, "Failed to get local tasks: ${it.message}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting local tasks", e)
            emptyList()
        }
    }

    private suspend fun syncOnlineTasks(inspectorId: String) {
        try {
            val result = taskRepository.getTasksByInspectorId(inspectorId)
            result.fold(
                onSuccess = { tasks ->
                    Log.d(TAG, "Successfully synced ${tasks.size} tasks")
                    updateTasksDisplay(tasks)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showError = false,
                        offlineTasksCount = tasks.size
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to sync online tasks", error)
                    if (_filteredTasks.value.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            showError = true,
                            errorMessage = "Không thể đồng bộ task: ${error.message}"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception syncing online tasks", e)
        }
    }

    // ======= HÀM ĐÃ ĐƯỢC CẬP NHẬT LOGIC ĐỂ KIỂM TRA DRAFT =======
    private suspend fun updateTasksDisplay(tasks: List<Task>) = coroutineScope {
        val tasksWithDetailsDeferred = tasks.map { task ->
            async {
                val draftReportResult = reportRepository.getDraftReportByTaskId(task.taskId)
                val draftReport = draftReportResult.getOrNull()
                TaskWithDetails(
                    task = task,
                    branchName = "Branch ${task.branchId}",
                    supervisorName = "Supervisor ${task.supervisorId}",
                    draftReport = draftReport,
                    hasDraft = draftReport != null
                )
            }
        }
        allTasks = tasksWithDetailsDeferred.awaitAll()
        applyFilters()
    }

    private fun observeNetworkState() {
        viewModelScope.launch {
            networkMonitor.isConnected.collect { isConnected ->
                _networkState.value = isConnected
                if (isConnected) {
                    val currentUser = authRepository.getCurrentUser()
                    currentUser?.let { user ->
                        syncOnlineTasks(user.uId)
                    }
                }
            }
        }
    }

    private fun observeSyncProgress() {
        // Implementation for sync progress observation
    }

    fun getOfflineTasksInfo() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    val localTasks = getLocalTasks(currentUser.uId)
                    _uiState.value = _uiState.value.copy(offlineTasksCount = localTasks.size)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting offline tasks info", e)
            }
        }
    }

    fun refreshTasks() {
        loadTasks()
    }

    fun syncTasks() {
        viewModelScope.launch {
            _syncState.value = TaskSyncUiState.Syncing
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    val result = taskRepository.getTasksByInspectorId(currentUser.uId)
                    result.fold(
                        onSuccess = { tasks ->
                            updateTasksDisplay(tasks)
                            _syncState.value = TaskSyncUiState.Success(
                                message = "Tasks synced successfully",
                                taskCount = tasks.size
                            )
                            _uiState.value = _uiState.value.copy(offlineTasksCount = tasks.size)
                        },
                        onFailure = { error ->
                            _syncState.value = TaskSyncUiState.Error("Sync failed: ${error.message}")
                        }
                    )
                } else {
                    _syncState.value = TaskSyncUiState.Error("No authenticated user")
                }
            } catch (e: Exception) {
                _syncState.value = TaskSyncUiState.Error("Sync failed: ${e.message}")
            }
        }
    }

    fun clearSyncStatus() {
        _syncState.value = TaskSyncUiState.Idle
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(showError = false, errorMessage = "")
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun updatePriorityFilter(priority: String) {
        _uiState.value = _uiState.value.copy(selectedPriority = priority)
        applyFilters()
    }

    fun updateStatusFilter(status: String) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
        applyFilters()
    }

    fun updateDateFilter(dateFilter: String) {
        _uiState.value = _uiState.value.copy(selectedDateFilter = dateFilter)
        applyFilters()
    }

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        viewModelScope.launch {
            try {
                val result = taskRepository.updateTaskStatus(taskId, newStatus)
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "Task status updated successfully: $taskId -> $newStatus")
                        loadTasks() // Refresh tasks
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to update task status", error)
                        _uiState.value = _uiState.value.copy(showError = true, errorMessage = "Failed to update task status: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception updating task status", e)
                _uiState.value = _uiState.value.copy(showError = true, errorMessage = "Error updating task status: ${e.message}")
            }
        }
    }

    private fun applyFilters() {
        var filtered = allTasks
        val state = _uiState.value

        if (state.searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.task.title.contains(state.searchQuery, true) ||
                        it.task.description.contains(state.searchQuery, true)
            }
        }
        if (state.selectedPriority != "All") {
            filtered = filtered.filter { it.task.priority.name == state.selectedPriority }
        }
        if (state.selectedStatus != "All") {
            filtered = filtered.filter { it.task.status.name == state.selectedStatus }
        }
        // Date filter logic can be added here
        _filteredTasks.value = filtered
    }
}

// Data Classes
data class InspectorTaskUiState(
    val isLoading: Boolean = false,
    val showError: Boolean = false,
    val errorMessage: String = "",
    val searchQuery: String = "",
    val selectedPriority: String = "All",
    val selectedStatus: String = "All",
    val selectedDateFilter: String = "All",
    val offlineTasksCount: Int = 0
)

data class TaskWithDetails(
    val task: Task,
    val branchName: String = "",
    val supervisorName: String = "",
    val draftReport: Report? = null,
    val hasDraft: Boolean = false
)

sealed class TaskSyncUiState {
    object Idle : TaskSyncUiState()
    object Syncing : TaskSyncUiState()
    data class Success(val message: String, val taskCount: Int = 0) : TaskSyncUiState()
    data class Error(val message: String) : TaskSyncUiState()
}