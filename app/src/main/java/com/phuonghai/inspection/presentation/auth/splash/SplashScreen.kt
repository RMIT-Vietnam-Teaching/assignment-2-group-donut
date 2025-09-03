package com.phuonghai.inspection.presentation.auth.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.phuonghai.inspection.domain.model.UserRole
import com.phuonghai.inspection.presentation.navigation.Screen

@Composable
fun SplashScreen(navController: NavController) {
    // Sử dụng SplashViewModel mới
    val viewModel: SplashViewModel = hiltViewModel()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(isLoading, userRole) {
        // Chỉ điều hướng khi đã kiểm tra xong (isLoading = false)
        if (!isLoading) {
            when (userRole) {
                UserRole.INSPECTOR -> navController.navigate(Screen.InspectorDashboard.route) {
                    popUpTo("splash_screen") { inclusive = true }
                }
                UserRole.SUPERVISOR -> navController.navigate(Screen.SupervisorDashboard.route) {
                    popUpTo("splash_screen") { inclusive = true }
                }
                null -> navController.navigate(Screen.LoginScreen.route) {
                    popUpTo("splash_screen") { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}