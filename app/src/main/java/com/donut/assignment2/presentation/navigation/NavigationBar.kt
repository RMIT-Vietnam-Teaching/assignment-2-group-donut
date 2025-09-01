package com.example.campuscompanion.presentation.navigation


import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.donut.assignment2.presentation.inspector.profile.InspectorProfileScreen
import com.donut.assignment2.presentation.inspector.dashboard.InspectorDashboardScreen
import com.donut.assignment2.presentation.inspector.newreport.InspectorNewReportScreen
import com.donut.assignment2.presentation.inspector.notification.InspectorNotificationScreen
import com.donut.assignment2.presentation.navigation.Screen
import com.donut.assignment2.presentation.supervisor.dashboard.SupervisorDashboardScreen
import com.donut.assignment2.presentation.supervisor.history.SupervisorHistoryScreen
import com.donut.assignment2.presentation.supervisor.map.SupervisorMapScreen
import com.donut.assignment2.presentation.supervisor.profile.SupervisorProfileScreen


enum class InspectorDestination(
    val route: String,
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val contentDescription: String
) {
    DASHBOARDS(Screen.InspectorDashboardScreen.route, "Dashboard", Icons.Outlined.Home, Icons.Filled.Home,"Dashboard"),
    REPORTS(Screen.InspectorNewReportScreen.route, "New Reports", Icons.Outlined.Book, Icons.Filled.Book,"New Reports"),
    NOTIFICATIONS(Screen.InspectorNotificationScreen.route, "Notifications", Icons.Outlined.Notifications, Icons.Filled.Notifications,"Notifications"),
    PROFILES(Screen.InspectorProfileScreen.route, "Profiles", Icons.Outlined.Person, Icons.Filled.Person,"Profiles"),
}

enum class SupervisorDestination(
    val route: String,
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val contentDescription: String
) {
    DASHBOARD(Screen.SupervisorDashboardScreen.route, "Dashboard", Icons.Outlined.Home, Icons.Filled.Home, "Dashboard"),
    HISTORY(Screen.SupervisorHistoryScreen.route, "History", Icons.Outlined.History, Icons.Filled.History, "History"),
    MAP(Screen.SupervisorMapScreen.route, "Map", Icons.Outlined.Map, Icons.Filled.Map, "Map"),
    PROFILE(Screen.SupervisorProfileScreen.route, "Profile", Icons.Outlined.Person, Icons.Filled.Person, "Profile"),
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
                    InspectorDestination.DASHBOARDS -> InspectorDashboardScreen(navController = navController)
                    InspectorDestination.REPORTS -> InspectorNewReportScreen(navController = navController)
                    InspectorDestination.NOTIFICATIONS -> InspectorNotificationScreen(navController = navController)
                    InspectorDestination.PROFILES -> InspectorProfileScreen(navController = navController)
                }
            }
        }

    }
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
                    SupervisorDestination.DASHBOARD -> SupervisorDashboardScreen(navController = navController)
                    SupervisorDestination.HISTORY -> SupervisorHistoryScreen(navController = navController)
                    SupervisorDestination.MAP -> SupervisorMapScreen(navController = navController)
                    SupervisorDestination.PROFILE ->SupervisorProfileScreen(navController = navController)
                }
            }
        }

    }
}

@Composable
fun InspectorNavigationBar(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val startDestination = InspectorDestination.DASHBOARDS
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                InspectorDestination.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedDestination == index,
                        onClick = {
                            navController.navigate(route = destination.route)
                            selectedDestination = index
                        },
                        icon = {
                            val displayIcon = if(index == selectedDestination) destination.selectedIcon else destination.unselectedIcon
                            Icon(
                                displayIcon,
                                contentDescription = destination.contentDescription,
                                modifier = Modifier.size(40.dp)
                            )
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { contentPadding ->
        InspectorNavHost(navController, startDestination, modifier = Modifier.padding(contentPadding))
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
                            navController.navigate(route = destination.route)
                            selectedDestination = index
                        },
                        icon = {
                            val displayIcon = if(index == selectedDestination) destination.selectedIcon else destination.unselectedIcon
                            Icon(
                                displayIcon,
                                contentDescription = destination.contentDescription,
                                modifier = Modifier.size(40.dp)
                            )
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { contentPadding ->
        SupervisorNavHost(navController, startDestination, modifier = Modifier.padding(contentPadding))
    }
}


