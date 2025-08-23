package com.donut.assignment2

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FieldInspectionApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // App initialization
    }
}