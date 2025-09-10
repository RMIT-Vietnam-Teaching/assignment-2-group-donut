package com.phuonghai.inspection.core.network

import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.phuonghai.inspection.core.sync.TaskSyncService
import com.phuonghai.inspection.core.sync.ReportSyncService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NetworkConnectionListener : LifecycleService() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var taskSyncService: TaskSyncService

    @Inject
    lateinit var reportSyncService: ReportSyncService

    companion object {
        private const val TAG = "NetworkConnectionListener"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NetworkConnectionListener service created")
        startNetworkMonitoring()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "NetworkConnectionListener service destroyed")
    }

    private fun startNetworkMonitoring() {
        Log.d(TAG, "Starting network monitoring")

        lifecycleScope.launch {
            var previousState: Boolean? = null

            networkMonitor.isConnected.collect { isConnected ->
                // Only act on state changes
                if (previousState != null && previousState != isConnected) {
                    if (isConnected) {
                        Log.d(TAG, "Network connected - starting auto sync")
                        onNetworkConnected()
                    } else {
                        Log.d(TAG, "Network disconnected")
                        onNetworkDisconnected()
                    }
                }
                previousState = isConnected
            }
        }
    }

    private suspend fun onNetworkConnected() {
        try {
            Log.d(TAG, "Network is available - performing auto sync")

            // Auto sync tasks when network is available
            Log.d(TAG, "Starting automatic task sync...")
            val taskSyncResult = taskSyncService.autoSyncTasks()

            when (taskSyncResult) {
                is com.phuonghai.inspection.core.sync.TaskSyncResult.Success -> {
                    Log.d(TAG, "Task sync completed: ${taskSyncResult.taskCount} tasks synced")
                }
                is com.phuonghai.inspection.core.sync.TaskSyncResult.Error -> {
                    Log.e(TAG, "Task sync failed", taskSyncResult.exception)
                }
            }

            // Also sync pending reports - FIXED: Use syncAllPendingReports instead
            Log.d(TAG, "Starting automatic report sync...")
            val reportSyncResult = reportSyncService.syncAllPendingReports()

            when (reportSyncResult) {
                is com.phuonghai.inspection.core.sync.SyncResult.Success -> {
                    Log.d(TAG, "Report sync completed: ${reportSyncResult.syncedCount} reports synced")
                }
                is com.phuonghai.inspection.core.sync.SyncResult.Error -> {
                    Log.e(TAG, "Report sync failed", reportSyncResult.exception)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error during auto sync", e)
        }
    }

    private fun onNetworkDisconnected() {
        Log.d(TAG, "Network disconnected - switching to offline mode")
        // You can add any offline-specific logic here if needed
        // For example, showing notifications, updating UI state, etc.
    }
}