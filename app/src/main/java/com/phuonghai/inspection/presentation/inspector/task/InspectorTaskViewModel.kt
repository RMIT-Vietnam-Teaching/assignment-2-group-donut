package com.phuonghai.inspection.presentation.inspector.task

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phuonghai.inspection.domain.model.Task
import com.phuonghai.inspection.domain.model.TaskStatus
import com.phuonghai.inspection.domain.model.User
import com.phuonghai.inspection.domain.repository.IAuthRepository
import com.phuonghai.inspection.domain.repository.IBranchRepository
import com.phuonghai.inspection.domain.repository.IUserRepository
import com.phuonghai.inspection.domain.repository.IReportRepository
import com.phuonghai.inspection.domain.usecase.GetInspectorTasksUseCase
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
    private val getInspectorTasksUseCase: GetInspectorTasksUseCase,
    private val updateTaskStatusUseCase: UpdateTaskStatusUseCase,
    private val branchRepository: IBranchRepository,
    private val userRepository: IUserRepository,
    private val reportRepository: IReportRepository
) : ViewModel() {

    companion object {
        private const val TAG = "InspectorTaskVM"
    }

    private val _uiState = MutableStateFlow(InspectorTaskUiState())
    val uiState: StateFlow<InspectorTaskUiState> = _uiState.asStateFlow()

    // Filtered tasks based on search and filters
    private val _filteredTasks = MutableStateFlow<List<TaskWithDetails>>(emptyList())
    val filteredTasks: StateFlow<List<TaskWithDetails>> = _filteredTasks.asStateFlow()

    init {
        loadTasks()
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
                        errorMessage = "Không thể xác định inspector hiện tại"
                    )
                    return@launch
                }

                // Get inspector's tasks
                val tasksResult = getInspectorTasksUseCase(currentUser.uId)

                tasksResult.fold(
                    onSuccess = { tasks ->
                        Log.d(TAG, "Loaded ${tasks.size} tasks")

                        // Load additional details for each task
                        val tasksWithDetails = loadTaskDetails(tasks)

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            tasks = tasksWithDetails,
                            errorMessage = null
                        )

                        applyFilters()
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error loading tasks", error)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Lỗi tải tasks: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading tasks", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi không xác định: ${e.message}"
                )
            }
        }
    }

    private suspend fun loadTaskDetails(tasks: List<Task>): List<TaskWithDetails> {
        val tasksWithDetails = mutableListOf<TaskWithDetails>()

        // Get branches
        val branchesResult = branchRepository.getBranches()
        val branches = branchesResult.getOrNull() ?: emptyList()

        // Get supervisors - sử dụng getAllUsers thay vì getInspectors
        val usersResult = userRepository.getAllUsers()
        val allUsers = usersResult.getOrNull() ?: emptyList()

        // Get current inspector ID for draft reports
        val currentUser = authRepository.getCurrentUser()
        val inspectorId = currentUser?.uId ?: ""

        // Get draft reports for this inspector
        val draftReportsResult = reportRepository.getDraftReportsByInspectorId(inspectorId)
        val draftReports = draftReportsResult.getOrNull() ?: emptyList()

        tasks.forEach { task ->
            val branch = branches.find { it.branchId == task.branchId }
            val supervisor = allUsers.find { it.uId == task.supervisorId }

            // Check if this task has a draft report
            val hasDraft = draftReports.any { it.taskId == task.taskId }
            val draftReport = draftReports.find { it.taskId == task.taskId }

            tasksWithDetails.add(
                TaskWithDetails(
                    task = task,
                    branchName = branch?.branchName ?: "Unknown Branch",
                    supervisorName = supervisor?.fullName ?: "Unknown Supervisor",
                    hasDraft = hasDraft,
                    draftReport = draftReport
                )
            )
        }

        return tasksWithDetails
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

    private fun applyFilters() {
        val state = _uiState.value
        var filtered = state.tasks

        // Apply search filter
        if (state.searchQuery.isNotBlank()) {
            filtered = filtered.filter { taskWithDetails ->
                taskWithDetails.task.title.contains(state.searchQuery, ignoreCase = true) ||
                        taskWithDetails.task.description.contains(state.searchQuery, ignoreCase = true) ||
                        taskWithDetails.branchName.contains(state.searchQuery, ignoreCase = true)
            }
        }

        // Apply priority filter
        if (state.selectedPriority != "All") {
            filtered = filtered.filter { taskWithDetails ->
                taskWithDetails.task.priority.name == state.selectedPriority
            }
        }

        // Apply status filter
        if (state.selectedStatus != "All") {
            filtered = filtered.filter { taskWithDetails ->
                taskWithDetails.task.status.name == state.selectedStatus
            }
        }

        // Apply date filter
        filtered = when (state.selectedDateFilter) {
            "Today" -> {
                val today = System.currentTimeMillis()
                val todayStart = today - (today % (24 * 60 * 60 * 1000))
                filtered.filter { taskWithDetails ->
                    val taskTime = taskWithDetails.task.dueDate?.toDate()?.time ?: 0
                    taskTime >= todayStart && taskTime < todayStart + (24 * 60 * 60 * 1000)
                }
            }
            "This Week" -> {
                val today = System.currentTimeMillis()
                val weekStart = today - (7 * 24 * 60 * 60 * 1000)
                filtered.filter { taskWithDetails ->
                    val taskTime = taskWithDetails.task.dueDate?.toDate()?.time ?: 0
                    taskTime >= weekStart
                }
            }
            "This Month" -> {
                val today = System.currentTimeMillis()
                val monthStart = today - (30 * 24 * 60 * 60 * 1000)
                filtered.filter { taskWithDetails ->
                    val taskTime = taskWithDetails.task.dueDate?.toDate()?.time ?: 0
                    taskTime >= monthStart
                }
            }
            else -> filtered
        }

        _filteredTasks.value = filtered
        Log.d(TAG, "Applied filters: ${filtered.size} tasks after filtering")
    }

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        viewModelScope.launch {
            try {
                val result = updateTaskStatusUseCase(taskId, newStatus)
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "Task status updated successfully: $taskId -> $newStatus")
                        loadTasks() // Refresh the tasks
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error updating task status", error)
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Lỗi cập nhật trạng thái: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception updating task status", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Lỗi không xác định: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun refreshTasks() {
        Log.d(TAG, "Manual refresh requested")
        loadTasks()
    }

    // Helper method to get draft report for a specific task
    fun getDraftReportForTask(taskId: String): com.phuonghai.inspection.domain.model.Report? {
        return _uiState.value.tasks.find { it.task.taskId == taskId }?.draftReport
    }

    // Helper method to check if a task has draft
    fun taskHasDraft(taskId: String): Boolean {
        return _uiState.value.tasks.find { it.task.taskId == taskId }?.hasDraft ?: false
    }
}

data class InspectorTaskUiState(
    val isLoading: Boolean = false,
    val tasks: List<TaskWithDetails> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedPriority: String = "All",
    val selectedStatus: String = "All",
    val selectedDateFilter: String = "All"
) {
    val showContent: Boolean get() = !isLoading && tasks.isNotEmpty()
    val showError: Boolean get() = errorMessage != null
    val isEmpty: Boolean get() = !isLoading && tasks.isEmpty() && errorMessage == null
}

data class TaskWithDetails(
    val task: Task,
    val branchName: String,
    val supervisorName: String,
    val hasDraft: Boolean = false,
    val draftReport: com.phuonghai.inspection.domain.model.Report? = null
)