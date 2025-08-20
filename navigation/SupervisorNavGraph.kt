package com.example.ui_for_assignment2.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ui_for_assignment2.ui.screens.SupervisorHistoryScreen
import com.example.ui_for_assignment2.ui.screens.SupervisorMapScreen
import com.example.ui_for_assignment2.ui.screens.SupervisorPendingScreen
import com.example.ui_for_assignment2.ui.screens.SupervisorSettingsScreen

@Composable
fun SupervisorNavGraph(
    navController: NavHostController,
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "SupervisorHistoryScreen",
        modifier = modifier
    ) {
        composable("SupervisorHistoryScreen") {
            SupervisorHistoryScreen()
        }
        composable("SupervisorMapScreen") {
            SupervisorMapScreen()
        }
        composable("SupervisorPendingScreen") {
            SupervisorPendingScreen()
        }
        composable("SupervisorSettingsScreen") {
            SupervisorSettingsScreen(
                darkTheme = darkTheme,
                onThemeChange = onThemeChange
            )
        }
    }
}
