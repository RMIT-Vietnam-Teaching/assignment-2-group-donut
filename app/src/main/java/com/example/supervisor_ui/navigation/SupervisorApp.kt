package com.example.supervisor_ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController

@Composable
fun SupervisorApp(
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    SupervisorNavGraph(
        navController = navController,
        darkTheme = darkTheme,
        onThemeChange = onThemeChange,
        modifier = modifier
    )
}