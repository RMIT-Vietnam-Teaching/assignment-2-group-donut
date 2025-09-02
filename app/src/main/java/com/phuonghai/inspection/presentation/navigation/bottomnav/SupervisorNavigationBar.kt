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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.donut.assignment2.presentation.supervisor.map.SupervisorMapScreen
import com.phuonghai.inspection.presentation.home.supervisor.SupervisorDashboard

enum class SupervisorDestination(
    val route: String,
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val contentDescription: String
) {
    DASHBOARD("supervisor_dashboard", "Dashboard", Icons.Outlined.Home, Icons.Filled.Home, "Dashboard"),
    HISTORY("supervisor_history", "History", Icons.Outlined.History, Icons.Filled.History, "History"),
    MAP("supervisor_map","map", Icons.Outlined.Map, Icons.Filled.Map,"Map"),
    PROFILE("supervisor_profile", "Profile", Icons.Outlined.Person, Icons.Filled.Person, "Profile")
}

@Composable
fun SupervisorNavHost(
    navController: NavHostController,
    startDestination: SupervisorDestination,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController,
        startDestination = startDestination.route
    ) {
        SupervisorDestination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    SupervisorDestination.DASHBOARD -> SupervisorDashboard()
                    SupervisorDestination.HISTORY -> SupervisorHistoryScreen()
                    SupervisorDestination.MAP -> SupervisorMapScreen()
                    SupervisorDestination.PROFILE -> SupervisorProfileScreen()
                }
            }
        }
    }
}

@Composable
fun SupervisorNavigationBar(modifier: Modifier = Modifier) {
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
            navController,
            startDestination,
            modifier = Modifier.padding(contentPadding)
        )
    }
}

@Composable
fun SupervisorHistoryScreen() {
    // Placeholder screen
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("Supervisor History Screen")
    }
}

@Composable
fun SupervisorProfileScreen() {
    // Placeholder screen
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("Supervisor Profile Screen")
    }
}
@Composable
fun SupervisorMapScreen() {
    // Placeholder screen
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("Supervisor Map Screen")
    }
}