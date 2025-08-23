package com.example.supervisor_ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.supervisor_ui.screens.SupervisorHistoryScreen
import com.example.supervisor_ui.screens.SupervisorMapScreen
import com.example.supervisor_ui.screens.SupervisorPendingScreen
import com.example.supervisor_ui.screens.SupervisorSettingsScreen

@Composable
fun SupervisorNavGraph(
    navController: NavHostController,
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "SupervisorPendingScreen",
        modifier = modifier
    ) {
        composable("SupervisorHistoryScreen") {
            SupervisorHistoryScreen(navController)
        }
        composable("SupervisorMapScreen") {
            SupervisorMapScreen(navController)
        }
        composable("SupervisorPendingScreen") {
            SupervisorPendingScreen(navController)
        }
        composable("SupervisorSettingsScreen") {
            SupervisorSettingsScreen(
                navController,
                darkTheme = darkTheme,
                onThemeChange = onThemeChange
            )
        }
    }
}