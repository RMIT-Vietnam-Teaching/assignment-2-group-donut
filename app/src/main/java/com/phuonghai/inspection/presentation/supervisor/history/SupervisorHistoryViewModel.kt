package com.phuonghai.inspection.presentation.supervisor.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.phuonghai.inspection.domain.model.Report
import com.phuonghai.inspection.domain.usecase.GetReportsBySupervisorUseCase
import com.phuonghai.inspection.domain.usecase.GetUserInformationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class SupervisorHistoryViewModel @Inject constructor(
    private val getReportsBySupervisorUseCase: GetReportsBySupervisorUseCase,
    private val getUserInformationUseCase: GetUserInformationUseCase
) : ViewModel() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _reports = MutableStateFlow<List<ReportWithInspector>>(emptyList())
    val reports: StateFlow<List<ReportWithInspector>> = _reports.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    private val _supervisorName = MutableStateFlow<String>("")
    val supervisorName: StateFlow<String> = _supervisorName.asStateFlow()

    fun loadReports() {
        viewModelScope.launch {
            _isLoading.value = true
            getReportsBySupervisorUseCase(currentUserId)
                .onSuccess { reports ->
                    val enrichedReports = coroutineScope {
                        reports.map { report ->
                            async {
                                val inspector = getUserInformationUseCase(report.inspectorId).getOrNull()
                                ReportWithInspector(
                                    report = report,
                                    inspectorName = inspector?.fullName ?: "Unknown Inspector"
                                )
                            }
                        }.awaitAll()
                    }.filter { it.report.responseStatus.name != "PENDING" }

                    _reports.value = enrichedReports
                    _isLoading.value = false
                }
                .onFailure { e ->
                    Log.e("SupervisorHistoryViewModel", "Error loading reports", e)
                    _isLoading.value = false
                }
        }
    }
    fun loadSupervisorName(){
        viewModelScope.launch {
            getUserInformationUseCase(currentUserId)
                .onSuccess { user ->
                    if (user != null) {
                        _supervisorName.value = user.fullName
                    }
                }
                .onFailure { e ->
                    Log.e("SupervisorHistoryViewModel", "Error loading supervisor name", e)
                }
        }
    }

    init {
        loadSupervisorName()
        loadReports()
    }
}

data class ReportWithInspector(
    val report: Report,
    val inspectorName: String
)
