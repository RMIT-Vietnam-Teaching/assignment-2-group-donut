package com.phuonghai.inspection.presentation.supervisor.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phuonghai.inspection.domain.model.Branch
import com.phuonghai.inspection.domain.model.Notification
import com.phuonghai.inspection.domain.model.NotificationType
import com.phuonghai.inspection.domain.model.Task
import com.phuonghai.inspection.domain.model.User
import com.phuonghai.inspection.domain.usecase.AssignTaskUseCase
import com.phuonghai.inspection.domain.usecase.CreateNotificationUseCase
import com.phuonghai.inspection.domain.usecase.GetBranchesUseCase
import com.phuonghai.inspection.domain.usecase.GetInspectorsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupervisorTaskViewModel @Inject constructor(
    private val assignTaskUseCase: AssignTaskUseCase,
    private val getBranchesUseCase: GetBranchesUseCase,
    private val getInspectorsUseCase: GetInspectorsUseCase,
    private val createNotificationUseCase: CreateNotificationUseCase
) : ViewModel() {

    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    val branches: StateFlow<List<Branch>> = _branches

    private val _inspectors = MutableStateFlow<List<User>>(emptyList())
    val inspectors: StateFlow<List<User>> = _inspectors

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    private val _success = MutableStateFlow<Boolean?>(null)
    val success: StateFlow<Boolean?> = _success
    init {
        loadBranches()
        loadInspectors()
    }

    private fun loadBranches() {
        viewModelScope.launch {
            getBranchesUseCase()
                .onSuccess { _branches.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    private fun loadInspectors() {
        viewModelScope.launch {
            getInspectorsUseCase()
                .onSuccess { _inspectors.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    fun assignTask(task: Task) {
        viewModelScope.launch {
            try {
                assignTaskUseCase(task)
                // Create notification for inspector
                val notification = Notification(
                    id = task.taskId, // or UUID.randomUUID().toString()
                    title = "New Task Assigned",
                    message = "You have been assigned a new task: ${task.title}",
                    date = task.createdAt,
                    senderId = task.supervisorId,
                    receiverId = task.inspectorId,
                    type = NotificationType.TASK_ASSIGNED
                )
                createNotificationUseCase(notification)
                _success.value = true
            } catch (e: Exception) {
                _error.value = e.message
                _success.value = false
            }
        }
    }
    fun resetSuccess() {
        _success.value = null
    }
}
sealed class TaskUiState {
    object Idle : TaskUiState()
    object Loading : TaskUiState()
    object Success : TaskUiState()
    data class Error(val message: String) : TaskUiState()
}