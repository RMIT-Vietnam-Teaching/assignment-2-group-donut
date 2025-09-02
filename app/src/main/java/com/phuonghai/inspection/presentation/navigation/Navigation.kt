package com.phuonghai.inspection.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.phuonghai.inspection.domain.model.UserRole
import com.phuonghai.inspection.presentation.auth.OtpScreen
import com.phuonghai.inspection.presentation.auth.PhoneLoginScreen
import com.phuonghai.inspection.presentation.auth.splash.SplashScreen
import com.phuonghai.inspection.presentation.navigation.bottomnav.InspectorNavigationBar
import com.phuonghai.inspection.presentation.navigation.bottomnav.SupervisorNavigationBar

@Composable
fun InspectionAppNavigation() {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) Screen.Main.route else Screen.SplashScreen.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable(Screen.SplashScreen.route) {
            SplashScreen(navController = navController)
        }

        // Login Screen
        composable(Screen.LoginScreen.route) {
            PhoneLoginScreen(navController = navController)
        }

        // OTP Screen
        composable(
            route = Screen.OTPScreen.route,
            arguments = listOf(navArgument("verificationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val verificationId = backStackEntry.arguments?.getString("verificationId") ?: ""
            OtpScreen(navController = navController, verificationId = verificationId)
        }

        // Main Screen - determines role and shows appropriate navigation
        composable(Screen.Main.route) {
            val mainViewModel: MainViewModel = hiltViewModel()
            val currentUser = FirebaseAuth.getInstance().currentUser
            val phoneNumber = currentUser?.phoneNumber
            val role by mainViewModel.userRole.collectAsState()

            LaunchedEffect(phoneNumber) {
                if (phoneNumber != null) {
                    mainViewModel.loadUserRole(phoneNumber)
                }
            }

            when (role) {
                UserRole.INSPECTOR -> InspectorNavigationBar()
                UserRole.SUPERVISOR -> SupervisorNavigationBar()
                null -> {
                    // Show loading or redirect to login
                }
            }
        }
    }
}