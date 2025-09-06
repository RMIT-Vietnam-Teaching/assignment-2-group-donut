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
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.donut.assignment2.presentation.supervisor.history.SupervisorHistoryScreen
import com.donut.assignment2.presentation.supervisor.map.SupervisorMapScreen
import com.donut.assignment2.presentation.supervisor.profile.SupervisorProfileScreen
import com.phuonghai.inspection.presentation.home.supervisor.SupervisorDashboardScreen
import com.phuonghai.inspection.presentation.navigation.Screen
import com.phuonghai.inspection.presentation.supervisor.chatbox.SupervisorChatBoxScreen
import com.phuonghai.inspection.presentation.supervisor.chatbox.SupervisorChatDetailScreen
import com.phuonghai.inspection.presentation.supervisor.report.SupervisorReportDetailScreen
import com.phuonghai.inspection.presentation.supervisor.task.SupervisorTaskScreen

enum class SupervisorDestination(
    val route: String,
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val contentDescription: String
) {
    DASHBOARD(Screen.SupervisorDashboard.route, "Dashboard", Icons.Outlined.Home, Icons.Filled.Home, "Dashboard"),
    TASK(Screen.SupervisorTaskScreen.route, "Task", Icons.Outlined.Task, Icons.Filled.Task, "Task"),
    HISTORY(Screen.SupervisorHistoryScreen.route, "History", Icons.Outlined.History, Icons.Filled.History, "History"),
    MAP(Screen.SupervisorMapScreen.route,"Map", Icons.Outlined.Map, Icons.Filled.Map,"Map"),
    PROFILE(Screen.SupervisorProfileScreen.route, "Profile", Icons.Outlined.Person, Icons.Filled.Person, "Profile")
}

@Composable
fun SupervisorNavHost(
    navController: NavHostController,
    startDestination: SupervisorDestination,
    rootNavController: NavController,
    modifier: Modifier = Modifier
) {
    NavHost(navController, startDestination = startDestination.route) {
        SupervisorDestination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    SupervisorDestination.DASHBOARD -> SupervisorDashboardScreen(navController = navController)
                    SupervisorDestination.TASK -> SupervisorTaskScreen()
                    SupervisorDestination.HISTORY -> SupervisorHistoryScreen(navController = navController)
                    SupervisorDestination.MAP -> SupervisorMapScreen(navController = navController)
                    SupervisorDestination.PROFILE -> SupervisorProfileScreen(
                        navController = rootNavController
                    )
                }
            }

        }
        composable(Screen.SupervisorReportDetailScreen.route + "/{reportId}") {
            backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId")
            if (reportId != null) {
                SupervisorReportDetailScreen(reportId = reportId, navController = navController)
            }
        }
        composable(Screen.SupervisorChatBoxScreen.route){
            SupervisorChatBoxScreen(navController = navController)
        }
        composable(Screen.SupervisorChatDetailScreen.route +"/{inspectorId}") { backStackEntry ->
            val inspectorId = backStackEntry.arguments?.getString("inspectorId")
            SupervisorChatDetailScreen(inspectorId = inspectorId ?: "", navController = navController)
        }
    }
}

@Composable
fun SupervisorNavigationBar(
    rootNavController: androidx.navigation.NavController,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val startDestination = SupervisorDestination.DASHBOARD
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                SupervisorDestination.entries.forEachIndexed { index, destination ->
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
        SupervisorNavHost(
            navController = navController,
            startDestination = startDestination,
            rootNavController = rootNavController,
            modifier = Modifier.padding(contentPadding)
        )
    }
}

