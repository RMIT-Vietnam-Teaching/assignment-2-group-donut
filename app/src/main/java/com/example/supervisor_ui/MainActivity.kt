package com.example.supervisor_ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.supervisor_ui.navigation.SupervisorApp
import com.example.supervisor_ui.ui.theme.Supervisor_uiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkTheme by remember { mutableStateOf(false) }
            Supervisor_uiTheme(darkTheme = darkTheme) {
                SupervisorApp(
                    darkTheme = darkTheme,
                    onThemeChange = { darkTheme = it }
                )
            }
        }
    }
}

