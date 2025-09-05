package com.phuonghai.inspection.presentation.supervisor.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.phuonghai.inspection.domain.model.Report
import com.phuonghai.inspection.domain.usecase.GetReportsBySupervisorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupervisorHistoryViewModel @Inject constructor(
    private val getReportsBySupervisorUseCase: GetReportsBySupervisorUseCase
) : ViewModel() {
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    fun loadReports() {
        viewModelScope.launch {
            _isLoading.value = true
            getReportsBySupervisorUseCase(currentUserId)
                .onSuccess { reports ->
                    // find the report already checked by supervisor
                    _reports.value = reports.filter { it.responseStatus.name !== "PENDING" }
                    _isLoading.value = false
                }
                .onFailure { e ->
                    Log.e("SupervisorDashboardViewModel", "Error loading reports", e)
                    _isLoading.value = false
                }
        }
    }
    init{
        loadReports()
    }
}