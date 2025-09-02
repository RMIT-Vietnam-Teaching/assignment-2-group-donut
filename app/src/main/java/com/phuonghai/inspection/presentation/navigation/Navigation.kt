package com.phuonghai.inspection.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.phuonghai.inspection.presentation.auth.OtpScreen
import com.phuonghai.inspection.presentation.auth.PhoneLoginScreen
import com.phuonghai.inspection.presentation.auth.splash.SplashScreen
import com.phuonghai.inspection.presentation.navigation.bottomnav.InspectorNavigationBar
import com.phuonghai.inspection.presentation.navigation.bottomnav.SupervisorNavigationBar

@Composable
fun InspectionAppNavigation() {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
    // ✅ Always start with splash to determine proper route
    val startDestination = Screen.SplashScreen.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen - determines where to go next
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

        // ✅ Direct role-based screens (no intermediate main screen)
        composable(Screen.InspectorDashboard.route) {
            InspectorNavigationBar()
        }

        composable(Screen.SupervisorDashboard.route) {
            SupervisorNavigationBar()
        }
    }
}