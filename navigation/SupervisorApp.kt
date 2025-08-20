package com.example.ui_for_assignment2.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui_for_assignment2.ui.theme.UI_for_assignment2Theme

@Composable
fun SupervisorApp() {
    var darkTheme by remember { mutableStateOf(false) }
    val navController = rememberNavController()

    UI_for_assignment2Theme(darkTheme = darkTheme) {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController = navController)
            }
        ) { innerPadding ->
            SupervisorNavGraph(
                navController = navController,
                darkTheme = darkTheme,
                onThemeChange = { darkTheme = it },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("History", "SupervisorHistoryScreen", Icons.Default.History),
        BottomNavItem("Map", "SupervisorMapScreen", Icons.Default.Map),
        BottomNavItem("Pending", "SupervisorPendingScreen", Icons.Default.Pending),
        BottomNavItem("Settings", "SupervisorSettingsScreen", Icons.Default.Settings),
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentDestination == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(val title: String, val route: String, val icon: ImageVector)
