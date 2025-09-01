package com.donut.assignment2.presentation.navigation


import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.donut.assignment2.domain.model.UserRole
import com.donut.assignment2.presentation.MainViewModel
import com.donut.assignment2.presentation.auth.login.OTPVerificationScreen
import com.donut.assignment2.presentation.auth.login.PhoneInputScreen
import com.example.campuscompanion.presentation.navigation.InspectorNavigationBar
import com.example.campuscompanion.presentation.navigation.SupervisorNavigationBar
import com.google.firebase.auth.FirebaseAuth


@Composable
fun Navigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) Screen.Main.route else Screen.PhoneInputScreen.route

    NavHost(navController = navController, startDestination = startDestination) {

        // Phone input
        composable(route = Screen.PhoneInputScreen.route) {
            PhoneInputScreen(
                onOTPSent = { verificationId ->
                    navController.navigate(Screen.OTPVerification.createRoute(verificationId))
                }
            )
        }

        // OTP Verification
        composable(
            route = Screen.OTPVerification.route,
            arguments = listOf(navArgument("verificationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val verificationId = backStackEntry.arguments?.getString("verificationId") ?: ""
            OTPVerificationScreen(
                verificationId = verificationId,
                onVerificationSuccess = { user ->
                    // After OTP success → navigate to main and clear back stack
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.PhoneInputScreen.route) { inclusive = true }
                    }
                },
                onBackToPhone = {
                    navController.popBackStack() // go back to phone input
                }
            )
        }

        // Main screen
        composable(route = Screen.Main.route) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val phoneNumber = currentUser?.phoneNumber

            val viewModel: MainViewModel = hiltViewModel()
            val role by viewModel.role.collectAsState()
            LaunchedEffect(role) {
                Log.d("MainScreen", "Current role: $role")
            }
            LaunchedEffect(phoneNumber) {
                if (phoneNumber != null) {
                    viewModel.loadUser(phoneNumber)
                }
            }

            when (role) {
                UserRole.INSPECTOR -> InspectorNavigationBar()
                UserRole.SUPERVISOR -> SupervisorNavigationBar()
                null -> { /* Show loading or placeholder */}
            }
        }
    }
}
