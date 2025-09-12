package com.phuonghai.inspection.presentation.inspector.historyreport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.phuonghai.inspection.domain.model.Report
import com.phuonghai.inspection.domain.repository.IReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class InspectorHistoryViewModel @Inject constructor(
    private val reportRepository: IReportRepository // Chỉ cần repository này
) : ViewModel() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Lắng nghe trực tiếp từ repository
    val reports: StateFlow<List<Report>> =
        if (currentUserId.isNotBlank()) {
            reportRepository.getReportsByInspectorId(currentUserId)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
        } else {
            MutableStateFlow(emptyList())
        }
}