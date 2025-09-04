package com.phuonghai.inspection.presentation.navigation.bottomnav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.donut.assignment2.presentation.inspector.history.InspectorHistoryReportScreen
import com.phuonghai.inspection.presentation.home.inspector.InspectorDashboard
import com.phuonghai.inspection.presentation.home.inspector.InspectorNewReportScreen
import com.phuonghai.inspection.presentation.home.inspector.InspectorNotificationScreen
import com.phuonghai.inspection.presentation.home.inspector.InspectorProfileScreen
import com.phuonghai.inspection.presentation.inspector.task.InspectorTaskScreen
import com.phuonghai.inspection.presentation.navigation.Screen

enum class InspectorDestination(
    val route: String,
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val contentDescription: String
) {
    DASHBOARD(Screen.InspectorDashboard.route, "Dashboard", Icons.Outlined.Home, Icons.Filled.Home, "Dashboard"),
    Task(Screen.InspectorTaskScreen.route, "Task", Icons.Outlined.Task, Icons.Filled.Task, "Task"),
    HISTORY(Screen.InspectorHistoryScreen.route, "History", Icons.Outlined.History, Icons.Filled.History, "History"),
    NOTIFICATIONS(Screen.InspectorNotificationScreen.route, "Notifications", Icons.Outlined.Notifications, Icons.Filled.Notifications, "Notifications"),
    PROFILE(Screen.InspectorProfileScreen.route, "Profile", Icons.Outlined.Person, Icons.Filled.Person, "Profile")
}

@Composable
fun InspectorNavHost(
    navController: NavHostController,
    startDestination: InspectorDestination,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController,
        startDestination = startDestination.route
    ) {
        InspectorDestination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    InspectorDestination.DASHBOARD -> InspectorDashboard()
                    InspectorDestination.Task -> InspectorTaskScreen(navController = navController)
                    InspectorDestination.HISTORY -> InspectorHistoryReportScreen(navController = navController)
                    InspectorDestination.NOTIFICATIONS -> InspectorNotificationScreen()
                    InspectorDestination.PROFILE -> InspectorProfileScreen(navController = navController)
                }
            }
        }

        // Updated composable with parameters support
        composable(
            route = "${Screen.InspectorNewReportScreen.route}?taskId={taskId}&reportId={reportId}",
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("reportId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            val reportId = backStackEntry.arguments?.getString("reportId")
            InspectorNewReportScreen(
                navController = navController,
                taskId = taskId,
                reportId = reportId
            )
        }
    }
}

@Composable
fun InspectorNavigationBar(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val startDestination = InspectorDestination.DASHBOARD
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                InspectorDestination.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedDestination == index,
                        onClick = {
                            if (selectedDestination != index) {
                                navController.navigate(route = destination.route)
                                selectedDestination = index
                            }
                        },
                        icon = {
                            val displayIcon = if (index == selectedDestination)
                                destination.selectedIcon else destination.unselectedIcon
                            Icon(
                                displayIcon,
                                contentDescription = destination.contentDescription,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { contentPadding ->
        InspectorNavHost(
            navController,
            startDestination,
            modifier = Modifier.padding(contentPadding)
        )
    }
}