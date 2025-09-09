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
import com.phuonghai.inspection.domain.usecase.UpdateTaskStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InspectorTaskViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val taskRepository: ITaskRepository,
    private val updateTaskStatusUseCase: UpdateTaskStatusUseCase,
    private val branchRepository: IBranchRepository,
    private val userRepository: IUserRepository,
    private val reportRepository: IReportRepository,
    private val taskSyncService: TaskSyncService,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    companion object {
        private const val TAG = "InspectorTaskVM"
    }

    private val _uiState = MutableStateFlow(InspectorTaskUiState())
    val uiState: StateFlow<InspectorTaskUiState> = _uiState.asStateFlow()

    private val _filteredTasks = MutableStateFlow<List<TaskWithDetails>>(emptyList())
    val filteredTasks: StateFlow<List<TaskWithDetails>> = _filteredTasks.asStateFlow()

    private val _syncState = MutableStateFlow<TaskSyncUiState>(TaskSyncUiState.Idle)
    val syncState: StateFlow<TaskSyncUiState> = _syncState.asStateFlow()

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

                        // Load additional details for each task including draft reports
                        val tasksWithDetails = loadTaskDetails(tasks)

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

    private suspend fun loadTaskDetails(tasks: List<Task>): List<TaskWithDetails> {
        val tasksWithDetails = mutableListOf<TaskWithDetails>()

        // Get branches
        val branchesResult = branchRepository.getBranches()
        val branches = branchesResult.getOrNull() ?: emptyList()

        // Get supervisors
        val usersResult = userRepository.getAllUsers()
        val allUsers = usersResult.getOrNull() ?: emptyList()

        // Process each task individually to get latest draft report for each task
        tasks.forEach { task ->
            val branch = branches.find { it.branchId == task.branchId }
            val supervisor = allUsers.find { it.uId == task.supervisorId }

            // Get latest draft report for this specific task
            val draftReportsResult = reportRepository.getDraftReportByTaskId(task.taskId)
            val latestDraftReport = draftReportsResult.getOrNull()

            Log.d(TAG, "Task ${task.taskId} - Latest draft: ${latestDraftReport?.reportId}")

            tasksWithDetails.add(
                TaskWithDetails(
                    task = task,
                    branchName = branch?.branchName ?: "Unknown Branch",
                    supervisorName = supervisor?.fullName ?: "Unknown Supervisor",
                    hasDraft = latestDraftReport != null,
                    draftReport = latestDraftReport
                )
            )
        }

        return tasksWithDetails
    }

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

    fun getOfflineTasksInfo() {
        viewModelScope.launch {
            val info = taskSyncService.getOfflineTasksInfo()
            _uiState.value = _uiState.value.copy(
                offlineTasksCount = info.totalTasks,
                hasOfflineTasks = info.isAvailable
            )
        }
    }

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

    private fun observeNetworkState() {
        viewModelScope.launch {
            var previousState: Boolean? = null

            networkMonitor.isConnected.collect { isConnected ->
                // Only act on state changes
                if (previousState != null && previousState != isConnected) {
                    _networkState.value = isConnected
                    _uiState.value = _uiState.value.copy(isOnline = isConnected)

                    if (isConnected) {
                        Log.d(TAG, "Network connected - auto syncing tasks")
                        // Auto sync when network becomes available
                        syncTasks()
                    }
                }
                previousState = isConnected
            }
        }
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
                        taskWithDetails.branchName.contains(currentState.searchQuery, ignoreCase = true)
            }
        }

        // Apply priority filter
        if (currentState.selectedPriority != "All") {
            filtered = filtered.filter {
                it.task.priority.name == currentState.selectedPriority
            }
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
        Log.d(TAG, "Applied filters: ${filtered.size} tasks after filtering")
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(showError = false, errorMessage = null)
    }

    fun clearSyncStatus() {
        _syncState.value = TaskSyncUiState.Idle
    }

    // Helper methods for draft functionality
    fun getDraftReportForTask(taskId: String): com.phuonghai.inspection.domain.model.Report? {
        return _uiState.value.tasks.find { it.task.taskId == taskId }?.draftReport
    }

    fun taskHasDraft(taskId: String): Boolean {
        return _uiState.value.tasks.find { it.task.taskId == taskId }?.hasDraft ?: false
    }
}

// UI State combining both sync and draft functionality
data class InspectorTaskUiState(
    val isLoading: Boolean = false,
    val tasks: List<TaskWithDetails> = emptyList(),
    val searchQuery: String = "",
    val selectedPriority: String = "All",
    val selectedStatus: String = "All",
    val selectedDateFilter: String = "All",
    val showError: Boolean = false,
    val errorMessage: String? = null,
    val offlineTasksCount: Int = 0,
    val hasOfflineTasks: Boolean = false,
    val isOnline: Boolean = true
) {
    val showContent: Boolean get() = !isLoading && tasks.isNotEmpty()
    val isEmpty: Boolean get() = !isLoading && tasks.isEmpty() && errorMessage == null
}

// Sync UI State
sealed class TaskSyncUiState {
    object Idle : TaskSyncUiState()
    object Syncing : TaskSyncUiState()
    data class Success(val taskCount: Int) : TaskSyncUiState()
    data class Error(val message: String) : TaskSyncUiState()
}

// TaskWithDetails with draft support
data class TaskWithDetails(
    val task: Task,
    val branchName: String,
    val supervisorName: String,
    val hasDraft: Boolean = false,
    val draftReport: com.phuonghai.inspection.domain.model.Report? = null
)