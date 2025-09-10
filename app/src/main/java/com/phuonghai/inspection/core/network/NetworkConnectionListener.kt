package com.phuonghai.inspection.core.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.phuonghai.inspection.R
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
        private const val CHANNEL_ID = "network_connection_listener"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NetworkConnectionListener service created")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        startNetworkMonitoring()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "NetworkConnectionListener service destroyed")
    }

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        // Keep service running if the system kills it
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Network Monitor",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Network connectivity monitoring"
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Monitoring network")
            .setContentText("Synchronizing when online")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
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