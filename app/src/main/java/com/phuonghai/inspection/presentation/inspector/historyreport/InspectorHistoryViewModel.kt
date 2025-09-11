package com.phuonghai.inspection.presentation.inspector.historyreport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.phuonghai.inspection.core.network.NetworkMonitor
import com.phuonghai.inspection.data.local.dao.LocalReportDao
import com.phuonghai.inspection.data.local.entity.toDomainModel
import com.phuonghai.inspection.domain.model.Report
import com.phuonghai.inspection.domain.repository.IReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class InspectorHistoryViewModel @Inject constructor(
    private val reportRepository: IReportRepository,
    private val localReportDao: LocalReportDao,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    companion object {
        private const val TAG = "InspectorHistoryViewModel"
    }

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _syncStatus = MutableStateFlow<HistorySyncStatus>(HistorySyncStatus.Idle)
    val syncStatus: StateFlow<HistorySyncStatus> = _syncStatus.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        observeNetworkStatus()
        loadHistoryReports()
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            networkMonitor.isConnected.collect { isConnected ->
                _isOnline.value = isConnected
                if (isConnected) {
                    Log.d(TAG, "Network connected - syncing with Firebase")
                    syncWithFirebase()
                }
            }
        }
    }

    fun loadHistoryReports() {
        Log.d(TAG, "Loading history reports for inspector: $currentUserId")

        if (currentUserId.isEmpty()) {
            _errorMessage.value = "Không thể xác định user hiện tại"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Luôn load local data trước để có gì hiển thị ngay
                loadLocalReports()

                // Nếu online thì sync với Firebase
                if (_isOnline.value) {
                    syncWithFirebase()
                } else {
                    Log.d(TAG, "Offline mode - only showing local reports")
                    _isLoading.value = false
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading history reports", e)
                _errorMessage.value = "Lỗi tải lịch sử báo cáo: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadLocalReports() {
        try {
            Log.d(TAG, "Loading local reports")

            localReportDao.getReportsByInspectorId(currentUserId).collect { localReports ->
                val domainReports = localReports.map { it.toDomainModel() }
                    .sortedByDescending { it.createdAt?.seconds }

                Log.d(TAG, "Found ${domainReports.size} local reports")

                // Merge với Firebase data nếu có
                if (_isOnline.value) {
                    // Nếu online, local data sẽ được merge với Firebase data
                    mergeWithCurrentReports(domainReports, isFromLocal = true)
                } else {
                    // Nếu offline, chỉ hiển thị local data
                    _reports.value = domainReports
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading local reports", e)
        }
    }

    private suspend fun syncWithFirebase() {
        try {
            _syncStatus.value = HistorySyncStatus.Syncing
            Log.d(TAG, "Syncing with Firebase")

            // Sử dụng flow để observe Firebase data
            reportRepository.getReportsByInspectorId(currentUserId).collect { firebaseReports ->
                Log.d(TAG, "Received ${firebaseReports.size} reports from Firebase")

                // Merge Firebase data với local data
                mergeWithCurrentReports(firebaseReports, isFromLocal = false)

                _syncStatus.value = HistorySyncStatus.Success
                _isLoading.value = false
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error syncing with Firebase", e)
            _syncStatus.value = HistorySyncStatus.Error("Lỗi đồng bộ: ${e.message}")
            _errorMessage.value = "Không thể đồng bộ với server: ${e.message}"
            _isLoading.value = false
        }
    }

    private fun mergeWithCurrentReports(newReports: List<Report>, isFromLocal: Boolean) {
        try {
            val map = mutableMapOf<String, Report>()

            _reports.value.forEach { report ->
                map[report.reportId] = report
            }

            newReports.forEach { report ->
                map[report.reportId] = chooseBetterReport(map[report.reportId], report, isFromLocal)
            }

            _reports.value = map.values.sortedByDescending { it.createdAt?.seconds ?: 0L }
            Log.d(TAG, "Merged reports - total: ${_reports.value.size}")

        } catch (e: Exception) {
            Log.e(TAG, "Error merging reports", e)
        }
    }

    private fun chooseBetterReport(existing: Report?, new: Report, newIsFromLocal: Boolean): Report {
        // Ưu tiên Firebase data nếu cả hai đều có
        // Nhưng nếu local có sync status là UNSYNCED thì giữ local

        val current = existing ?: return new

        return if (newIsFromLocal) {
            // Nếu data mới từ local và có sync status UNSYNCED, ưu tiên local
            if (new.syncStatus?.name == "UNSYNCED" || new.syncStatus?.name == "PENDING") {
                Log.d(TAG, "Keeping local unsynced report: ${new.reportId}")
                new
            } else {
                // Nếu local đã sync, ưu tiên Firebase (existing)
                current
            }
        } else {
            // Data mới từ Firebase - luôn ưu tiên Firebase trừ khi local chưa sync
            if (current.syncStatus?.name == "UNSYNCED" || current.syncStatus?.name == "PENDING") {
                Log.d(TAG, "Keeping local unsynced report over Firebase: ${current.reportId}")
                current
            } else {
                Log.d(TAG, "Using Firebase report: ${new.reportId}")
                new
            }
        }
    }

    fun refreshReports() {
        Log.d(TAG, "Manual refresh requested")
        loadHistoryReports()
    }

    fun retrySync() {
        if (_isOnline.value) {
            Log.d(TAG, "Retrying sync with Firebase")
            viewModelScope.launch {
                syncWithFirebase()
            }
        } else {
            _errorMessage.value = "Không có kết nối internet để đồng bộ"
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun getLocalReportsCount(): Int {
        return try {
            // This would need to be called from a coroutine
            0 // Placeholder - implement if needed for UI
        } catch (e: Exception) {
            Log.e(TAG, "Error getting local reports count", e)
            0
        }
    }

    fun getUnsyncedReportsCount(): Int {
        return _reports.value.count {
            it.syncStatus?.name == "UNSYNCED" || it.syncStatus?.name == "PENDING"
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared")
    }
}

sealed class HistorySyncStatus {
    object Idle : HistorySyncStatus()
    object Syncing : HistorySyncStatus()
    object Success : HistorySyncStatus()
    data class Error(val message: String) : HistorySyncStatus()
}

// Extension function to help with report comparison
private fun Report.isSameContent(other: Report): Boolean {
    return this.reportId == other.reportId &&
            this.title == other.title &&
            this.description == other.description &&
            this.score == other.score &&
            this.assignStatus == other.assignStatus &&
            this.responseStatus == other.responseStatus
}

// Data class for UI state if needed
data class HistoryUiState(
    val reports: List<Report> = emptyList(),
    val isLoading: Boolean = false,
    val isOnline: Boolean = false,
    val syncStatus: HistorySyncStatus = HistorySyncStatus.Idle,
    val errorMessage: String? = null,
    val localReportsCount: Int = 0,
    val unsyncedReportsCount: Int = 0
)