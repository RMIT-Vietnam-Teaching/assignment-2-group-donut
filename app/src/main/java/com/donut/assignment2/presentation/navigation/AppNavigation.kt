package com.donut.assignment2.presentation.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.donut.assignment2.domain.model.UserRole
import com.donut.assignment2.presentation.auth.login.OTPVerificationScreen
import com.donut.assignment2.presentation.auth.login.PhoneInputScreen
import com.donut.assignment2.presentation.inspector.dashboard.InspectorDashboardScreen
import com.donut.assignment2.presentation.supervisor.dashboard.SupervisorDashboardScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "phone_input"
    ) {
        // 1) Nhập số điện thoại
        composable(route = "phone_input") {
            Log.d("AppNavigation", "Navigating to phone_input screen")

            PhoneInputScreen(
                onOTPSent = { verificationId ->
                    Log.d("AppNavigation", "OTP sent, navigating to verification with ID: ${verificationId.take(10)}...")

                    // Kiểm tra verificationId trước khi navigate
                    if (verificationId.isNotBlank()) {
                        navController.navigate("otp_verification/$verificationId")
                    } else {
                        Log.e("AppNavigation", "VerificationId is blank, cannot navigate")
                    }
                }
            )
        }

        // 2) Màn nhập OTP - nhận verificationId qua NavArgs
        composable(
            route = "otp_verification/{verificationId}",
            arguments = listOf(
                navArgument("verificationId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val verificationId = backStackEntry.arguments?.getString("verificationId")

            Log.d("AppNavigation", "Navigating to OTP verification with ID: ${verificationId?.take(10)}...")

            if (verificationId.isNullOrBlank()) {
                Log.e("AppNavigation", "VerificationId is null/blank, navigating back to phone input")
                navController.navigate("phone_input") {
                    popUpTo("phone_input") { inclusive = true }
                }
                return@composable
            }

            OTPVerificationScreen(
                verificationId = verificationId,
                onVerificationSuccess = { user ->
                    Log.d("AppNavigation", "Verification successful for user: ${user.id} with role: ${user.role}")

                    val route = when (user.role) {
                        UserRole.INSPECTOR -> "inspector_dashboard/${user.id}"
                        UserRole.SUPERVISOR -> "supervisor_dashboard/${user.id}"
                    }

                    Log.d("AppNavigation", "Navigating to: $route")

                    navController.navigate(route) {
                        // Xóa màn login khỏi back stack
                        popUpTo("phone_input") { inclusive = true }
                    }
                },
                onBackToPhone = {
                    Log.d("AppNavigation", "Going back to phone input")
                    navController.navigate("phone_input") {
                        popUpTo("phone_input") { inclusive = true }
                    }
                }
            )
        }

        // 3) Dashboard Inspector
        composable(
            route = "inspector_dashboard/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")

            Log.d("AppNavigation", "Navigating to inspector dashboard for user: $userId")

            if (userId.isNullOrBlank()) {
                Log.e("AppNavigation", "UserId is null/blank, navigating back to phone input")
                navController.navigate("phone_input") {
                    popUpTo("phone_input") { inclusive = true }
                }
                return@composable
            }

            InspectorDashboardScreen(
                userId = userId,
                navController = navController
            )
        }

        // 4) Dashboard Supervisor
        composable(
            route = "supervisor_dashboard/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")

            Log.d("AppNavigation", "Navigating to supervisor dashboard for user: $userId")

            if (userId.isNullOrBlank()) {
                Log.e("AppNavigation", "UserId is null/blank, navigating back to phone input")
                navController.navigate("phone_input") {
                    popUpTo("phone_input") { inclusive = true }
                }
                return@composable
            }

            SupervisorDashboardScreen(
                userId = userId,
                navController = navController
            )
        }
    }
}