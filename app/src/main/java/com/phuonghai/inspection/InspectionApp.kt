package com.phuonghai.inspection

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.phuonghai.inspection.core.sync.SyncManager
import com.phuonghai.inspection.core.sync.TaskSyncWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class InspectionApp : Application() {

    @Inject
    lateinit var syncManager: SyncManager

    companion object {
        private const val TAG = "InspectionApp"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application started")

        // Initialize WorkManager manually (since we disabled auto-init)
        initializeWorkManager()

        // Initialize sync system
        initializeSync()
    }

    private fun initializeWorkManager() {
        try {
            val config = Configuration.Builder()
                .setMinimumLoggingLevel(Log.INFO)
                .build()

            WorkManager.initialize(this, config)
            Log.d(TAG, "WorkManager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing WorkManager", e)
        }
    }

    private fun initializeSync() {
        // Use coroutine scope to avoid blocking main thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Initialize sync manager
                syncManager.initialize()

                // Schedule periodic sync for tasks
                TaskSyncWorker.schedulePeriodicSync(this@InspectionApp)

                Log.d(TAG, "Sync system initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing sync system", e)
            }
        }
    }
}