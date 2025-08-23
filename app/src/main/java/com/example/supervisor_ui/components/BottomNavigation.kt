package com.example.supervisor_ui.components

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun BottomNavigation(
    navController: NavHostController,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    val screens = listOf(
        BottomNavScreen("History", "SupervisorHistoryScreen", Icons.Default.History),
        BottomNavScreen("Pending", "SupervisorPendingScreen", Icons.Default.Pending),
        BottomNavScreen("Map", "SupervisorMapScreen", Icons.Default.Map),
        BottomNavScreen("Settings", "SupervisorSettingsScreen", Icons.Default.Settings)
    )


    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        screens.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route){
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.id
                    )
                },
                label = {
                    Text(
                        text = screen.id,
                        fontSize = 12.sp
                    )
                }
            )
        }
    }
}

private data class BottomNavScreen(
    val id: String,
    val route: String,
    val icon: ImageVector
)