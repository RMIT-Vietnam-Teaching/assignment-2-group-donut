package com.example.supervisor_ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.supervisor_ui.components.BottomNavigation

@Composable
fun SupervisorMapScreen(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var activeScreen by remember { mutableStateOf("map") }

    Scaffold(
        bottomBar = {
            BottomNavigation(
                navController = navController,
                currentRoute = currentRoute,
                activeScreen = activeScreen,
                onScreenChange = { activeScreen = it }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Map Screen", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("This is where the Google Map view will go.")
        }
    }
}