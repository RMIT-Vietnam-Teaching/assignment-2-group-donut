package com.phuonghai.inspection.presentation.supervisor.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phuonghai.inspection.domain.model.Branch
import com.phuonghai.inspection.domain.model.Task
import com.phuonghai.inspection.domain.usecase.GetBranchesUseCase
import com.phuonghai.inspection.domain.usecase.GetTasksByBranchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupervisorMapViewModel @Inject constructor(
    private val getBranchesUseCase: GetBranchesUseCase,
    private val getTasksByBranchUseCase: GetTasksByBranchUseCase
) : ViewModel() {

    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    val branches: StateFlow<List<Branch>> = _branches

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    init {
        viewModelScope.launch {
            try {
                val result = getBranchesUseCase() // suspend call
                result.onSuccess { branchList ->
                    _branches.value = branchList
                }.onFailure { e ->
                    _error.value = e.message ?: "Unknown error"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    fun loadTasksByBranch(branchId: String) {
        viewModelScope.launch {
            try {
                val result = getTasksByBranchUseCase(branchId)
                result.onSuccess { taskList ->
                    _tasks.value = taskList
                }.onFailure { e ->
                    _error.value = e.message ?: "Unknown error"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}