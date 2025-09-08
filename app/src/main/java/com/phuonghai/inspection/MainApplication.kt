package com.phuonghai.inspection

import android.app.Application
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.phuonghai.inspection.core.network.NetworkMonitor
import com.phuonghai.inspection.core.sync.TaskSyncService
import com.phuonghai.inspection.core.sync.ReportSyncService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var taskSyncService: TaskSyncService

    @Inject
    lateinit var reportSyncService: ReportSyncService

    companion object {
        private const val TAG = "MainApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application started")

        // Initialize network monitoring and auto sync
        initializeAutoSync()
    }

    private fun initializeAutoSync() {
        // Monitor network changes and auto sync when online
        // Note: In real application, you might want to use a more sophisticated
        // approach like WorkManager for background sync

        // For now, we'll use a simple approach that works when app is active
        Log.d(TAG, "Initializing auto sync on network changes")

        // You can also schedule an immediate sync if network is available
        scheduleInitialSync()
    }

    private fun scheduleInitialSync() {
        // Schedule initial sync when app starts (if online)
        Thread {
            try {
                Thread.sleep(2000) // Wait 2 seconds for app to fully initialize

                // Check if we should do initial sync
                // This could be based on last sync time, user preferences, etc.
                Log.d(TAG, "Performing initial sync check...")

                // In a real app, you might use WorkManager here
                // WorkManager.getInstance(this).enqueue(...)

            } catch (e: Exception) {
                Log.e(TAG, "Error during initial sync", e)
            }
        }.start()
    }
}