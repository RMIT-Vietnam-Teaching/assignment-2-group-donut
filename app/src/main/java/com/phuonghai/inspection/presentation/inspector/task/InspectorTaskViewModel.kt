package com.phuonghai.inspection.presentation.inspector.task

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phuonghai.inspection.core.network.NetworkMonitor
import com.phuonghai.inspection.core.sync.TaskSyncService
import com.phuonghai.inspection.domain.model.Task
import com.phuonghai.inspection.domain.model.TaskStatus
import com.phuonghai.inspection.domain.model.User
import com.phuonghai.inspection.domain.repository.IAuthRepository
import com.phuonghai.inspection.domain.repository.IBranchRepository
import com.phuonghai.inspection.domain.repository.IUserRepository
import com.phuonghai.inspection.domain.repository.IReportRepository
import com.phuonghai.inspection.domain.repository.ITaskRepository
import com.phuonghai.inspection.domain.usecase.GetInspectorTasksUseCase
import com.phuonghai.inspection.domain.usecase.UpdateTaskStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InspectorTaskViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val taskRepository: ITaskRepository, // Now using OfflineTaskRepository
    private val updateTaskStatusUseCase: UpdateTaskStatusUseCase,
    private val branchRepository: IBranchRepository,
    private val userRepository: IUserRepository,
    private val reportRepository: IReportRepository,
    private val taskSyncService: TaskSyncService, // NEW: For sync functionality
    private val networkMonitor: NetworkMonitor // NEW: For network status
) : ViewModel() {

    companion object {
        private const val TAG = "InspectorTaskVM"
    }

    private val _uiState = MutableStateFlow(InspectorTaskUiState())
    val uiState: StateFlow<InspectorTaskUiState> = _uiState.asStateFlow()

    // Filtered tasks based on search and filters
    private val _filteredTasks = MutableStateFlow<List<TaskWithDetails>>(emptyList())
    val filteredTasks: StateFlow<List<TaskWithDetails>> = _filteredTasks.asStateFlow()

    // NEW: Sync state
    private val _syncState = MutableStateFlow<TaskSyncUiState>(TaskSyncUiState.Idle)
    val syncState: StateFlow<TaskSyncUiState> = _syncState.asStateFlow()

    // NEW: Network state
    private val _networkState = MutableStateFlow(false)
    val networkState: StateFlow<Boolean> = _networkState.asStateFlow()

    init {
        loadTasks()
        observeSyncProgress()
        observeNetworkState()
        getOfflineTasksInfo()
    }

    fun loadTasks() {
        Log.d(TAG, "Loading inspector tasks")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Get current inspector ID
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showError = true,
                        errorMessage = "Không thể xác định inspector hiện tại"
                    )
                    return@launch
                }

                // Get inspector's tasks (will use offline cache if available)
                val tasksResult = taskRepository.getTasksByInspectorId(currentUser.uId)

                tasksResult.fold(
                    onSuccess = { tasks ->
                        Log.d(TAG, "Loaded ${tasks.size} tasks")

                        // Load additional details for each task
                        val tasksWithDetails = tasks.map { task ->
                            loadTaskDetails(task)
                        }

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            tasks = tasksWithDetails,
                            showError = false,
                            errorMessage = null
                        )

                        // Apply current filters
                        applyFilters()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error loading tasks", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            showError = true,
                            errorMessage = "Lỗi khi tải danh sách task: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading tasks", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showError = true,
                    errorMessage = "Lỗi không mong muốn: ${e.message}"
                )
            }
        }
    }

    // NEW: Manual sync tasks
    fun syncTasks() {
        viewModelScope.launch {
            _syncState.value = TaskSyncUiState.Syncing

            val result = taskSyncService.autoSyncTasks()

            when (result) {
                is com.phuonghai.inspection.core.sync.TaskSyncResult.Success -> {
                    _syncState.value = TaskSyncUiState.Success(result.taskCount)
                    // Reload tasks after successful sync
                    loadTasks()
                }
                is com.phuonghai.inspection.core.sync.TaskSyncResult.Error -> {
                    _syncState.value = TaskSyncUiState.Error(result.exception.message ?: "Sync failed")
                }
            }
        }
    }

    // NEW: Get offline tasks info
    fun getOfflineTasksInfo() {
        viewModelScope.launch {
            val info = taskSyncService.getOfflineTasksInfo()
            _uiState.value = _uiState.value.copy(
                offlineTasksCount = info.totalTasks,
                hasOfflineTasks = info.isAvailable
            )
        }
    }

    // NEW: Observe sync progress
    private fun observeSyncProgress() {
        viewModelScope.launch {
            taskSyncService.syncProgress.collect { progress ->
                _syncState.value = when (progress) {
                    is com.phuonghai.inspection.core.sync.TaskSyncProgress.Idle -> TaskSyncUiState.Idle
                    is com.phuonghai.inspection.core.sync.TaskSyncProgress.InProgress -> TaskSyncUiState.Syncing
                    is com.phuonghai.inspection.core.sync.TaskSyncProgress.Completed -> TaskSyncUiState.Success(progress.taskCount)
                    is com.phuonghai.inspection.core.sync.TaskSyncProgress.Error -> TaskSyncUiState.Error(progress.message)
                }
            }
        }
    }

    // NEW: Observe network state
    private fun observeNetworkState() {
        viewModelScope.launch {
            networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConnected ->
                    _networkState.value = isConnected
                    _uiState.value = _uiState.value.copy(isOnline = isConnected)

                    if (isConnected) {
                        Log.d(TAG, "Network connected - auto syncing tasks")
                        // Auto sync when network becomes available
                        syncTasks()
                    }
                }
        }
    }

    private suspend fun loadTaskDetails(task: Task): TaskWithDetails {
        var branch: com.phuonghai.inspection.domain.model.Branch? = null
        var inspector: User? = null
        var hasReport = false

        try {
            // Load branch info
            branchRepository.getBranchById(task.branchId).fold(
                onSuccess = { branchData -> branch = branchData },
                onFailure = { Log.w(TAG, "Failed to load branch: ${task.branchId}") }
            )

            // Load inspector info
            userRepository.getUserById(task.inspectorId).fold(
                onSuccess = { userData -> inspector = userData },
                onFailure = { Log.w(TAG, "Failed to load inspector: ${task.inspectorId}") }
            )

            // Check if report exists for this task
            reportRepository.getDraftReportByTaskId(task.taskId).fold(
                onSuccess = { report -> hasReport = report != null },
                onFailure = { Log.w(TAG, "Failed to check report for task: ${task.taskId}") }
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error loading task details for ${task.taskId}", e)
        }

        return TaskWithDetails(
            task = task,
            branch = branch,
            inspector = inspector,
            hasReport = hasReport
        )
    }

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        viewModelScope.launch {
            try {
                updateTaskStatusUseCase(taskId, newStatus).fold(
                    onSuccess = {
                        Log.d(TAG, "Task status updated: $taskId -> $newStatus")
                        loadTasks() // Reload to reflect changes
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error updating task status", exception)
                        _uiState.value = _uiState.value.copy(
                            showError = true,
                            errorMessage = "Lỗi cập nhật trạng thái: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error updating task status", e)
                _uiState.value = _uiState.value.copy(
                    showError = true,
                    errorMessage = "Lỗi không mong muốn: ${e.message}"
                )
            }
        }
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

    fun refreshTasks() {
        loadTasks()
        if (_networkState.value) {
            syncTasks()
        }
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        var filtered = currentState.tasks

        // Apply search query
        if (currentState.searchQuery.isNotBlank()) {
            filtered = filtered.filter { taskWithDetails ->
                taskWithDetails.task.title.contains(currentState.searchQuery, ignoreCase = true) ||
                        taskWithDetails.task.description.contains(currentState.searchQuery, ignoreCase = true) ||
                        taskWithDetails.branch?.name?.contains(currentState.searchQuery, ignoreCase = true) == true
            }
        }

        // Apply priority filter
        if (currentState.selectedPriority != "All") {
            filtered = filtered.filter { it.task.priority == currentState.selectedPriority }
        }

        // Apply status filter
        if (currentState.selectedStatus != "All") {
            filtered = filtered.filter { it.task.status.name == currentState.selectedStatus }
        }

        // Apply date filter
        if (currentState.selectedDateFilter != "All") {
            val currentTime = System.currentTimeMillis()
            filtered = filtered.filter { taskWithDetails ->
                when (currentState.selectedDateFilter) {
                    "Today" -> {
                        val taskDueTime = taskWithDetails.task.dueDate?.seconds?.times(1000) ?: 0
                        val dayInMillis = 24 * 60 * 60 * 1000
                        Math.abs(currentTime - taskDueTime) < dayInMillis
                    }
                    "This Week" -> {
                        val taskDueTime = taskWithDetails.task.dueDate?.seconds?.times(1000) ?: 0
                        val weekInMillis = 7 * 24 * 60 * 60 * 1000
                        Math.abs(currentTime - taskDueTime) < weekInMillis
                    }
                    "This Month" -> {
                        val taskDueTime = taskWithDetails.task.dueDate?.seconds?.times(1000) ?: 0
                        val monthInMillis = 30L * 24 * 60 * 60 * 1000
                        Math.abs(currentTime - taskDueTime) < monthInMillis
                    }
                    else -> true
                }
            }
        }

        _filteredTasks.value = filtered
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(showError = false, errorMessage = null)
    }

    fun clearSyncStatus() {
        _syncState.value = TaskSyncUiState.Idle
    }
}

// Updated UI State with offline support
data class InspectorTaskUiState(
    val isLoading: Boolean = false,
    val tasks: List<TaskWithDetails> = emptyList(),
    val searchQuery: String = "",
    val selectedPriority: String = "All",
    val selectedStatus: String = "All",
    val selectedDateFilter: String = "All",
    val showError: Boolean = false,
    val errorMessage: String? = null,
    val offlineTasksCount: Int = 0, // NEW
    val hasOfflineTasks: Boolean = false, // NEW
    val isOnline: Boolean = true // NEW
)

// NEW: Sync UI State
sealed class TaskSyncUiState {
    object Idle : TaskSyncUiState()
    object Syncing : TaskSyncUiState()
    data class Success(val taskCount: Int) : TaskSyncUiState()
    data class Error(val message: String) : TaskSyncUiState()
}

data class TaskWithDetails(
    val task: Task,
    val branch: com.phuonghai.inspection.domain.model.Branch?,
    val inspector: User?,
    val hasReport: Boolean
)