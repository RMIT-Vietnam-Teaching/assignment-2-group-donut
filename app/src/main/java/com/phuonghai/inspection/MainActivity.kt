package com.phuonghai.inspection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.phuonghai.inspection.presentation.navigation.InspectionAppNavigation
import com.phuonghai.inspection.presentation.theme.FieldInspectionTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FieldInspectionTheme {
                InspectionAppNavigation()
            }
        }
    }
}