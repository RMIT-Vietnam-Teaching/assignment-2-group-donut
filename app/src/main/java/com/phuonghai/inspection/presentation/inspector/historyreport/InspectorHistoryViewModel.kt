package com.phuonghai.inspection.presentation.inspector.historyreport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.phuonghai.inspection.domain.model.Report
import com.phuonghai.inspection.domain.usecase.GetReportsByInspectorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InspectorHistoryViewModel @Inject constructor(
    private val getReportsByInspectorUseCase: GetReportsByInspectorUseCase
) : ViewModel() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    init {
        refreshReports()
    }

    private fun refreshReports() {
        viewModelScope.launch {
            getReportsByInspectorUseCase(currentUserId).collect { fetched ->
                _reports.value = fetched
            }
        }
    }
}
