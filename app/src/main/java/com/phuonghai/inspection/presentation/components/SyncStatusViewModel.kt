package com.phuonghai.inspection.presentation.components

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phuonghai.inspection.core.network.NetworkMonitor
import com.phuonghai.inspection.core.sync.ReportSyncService
import com.phuonghai.inspection.core.sync.SyncProgress
import com.phuonghai.inspection.core.sync.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncStatusViewModel @Inject constructor(
    private val syncService: ReportSyncService,
    private val networkMonitor: NetworkMonitor,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _syncProgress = MutableStateFlow<SyncProgress>(SyncProgress.Idle)
    val syncProgress: StateFlow<SyncProgress> = _syncProgress.asStateFlow()

    private val _unsyncedCount = MutableStateFlow(0)
    val unsyncedCount: StateFlow<Int> = _unsyncedCount.asStateFlow()

    private val _isNetworkConnected = MutableStateFlow(false)
    val isNetworkConnected: StateFlow<Boolean> = _isNetworkConnected.asStateFlow()

    companion object {
        private const val TAG = "SyncStatusViewModel"
    }

    init {
        // Monitor network status
        viewModelScope.launch {
            networkMonitor.isConnected.collect { isConnected ->
                _isNetworkConnected.value = isConnected
                if (isConnected) {
                    // Auto-sync when network becomes available
                    loadSyncStatus()
                    startAutoSync()
                }
            }
        }

        // Monitor sync progress
        viewModelScope.launch {
            syncService.syncProgress.collect { progress ->
                _syncProgress.value = progress

                // Update unsynced count after sync completion
                if (progress is SyncProgress.Completed) {
                    loadUnsyncedCount()
                }
            }
        }
    }

    fun loadSyncStatus() {
        viewModelScope.launch {
            try {
                loadUnsyncedCount()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading sync status", e)
            }
        }
    }

    private suspend fun loadUnsyncedCount() {
        try {
            val count = syncService.getUnsyncedReportsCount()
            _unsyncedCount.value = count
            Log.d(TAG, "Unsynced reports count: $count")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading unsynced count", e)
        }
    }

    fun startManualSync() {
        if (!_isNetworkConnected.value) {
            Log.d(TAG, "Cannot sync: No network connection")
            return
        }

        if (_syncProgress.value is SyncProgress.InProgress) {
            Log.d(TAG, "Sync already in progress")
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting manual sync")
                SyncWorker.scheduleImmediateSync(context)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting manual sync", e)
                _syncProgress.value = SyncProgress.Error("Failed to start sync: ${e.message}")
            }
        }
    }

    private fun startAutoSync() {
        if (!_isNetworkConnected.value) {
            return
        }

        viewModelScope.launch {
            try {
                val unsyncedCount = syncService.getUnsyncedReportsCount()
                if (unsyncedCount > 0) {
                    Log.d(TAG, "Starting auto sync for $unsyncedCount reports")
                    SyncWorker.scheduleImmediateSync(context)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting auto sync", e)
            }
        }
    }

    fun retryFailedSync() {
        if (_isNetworkConnected.value) {
            startManualSync()
        }
    }

    fun dismissSyncStatus() {
        if (_syncProgress.value !is SyncProgress.InProgress) {
            _syncProgress.value = SyncProgress.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "SyncStatusViewModel cleared")
    }
}