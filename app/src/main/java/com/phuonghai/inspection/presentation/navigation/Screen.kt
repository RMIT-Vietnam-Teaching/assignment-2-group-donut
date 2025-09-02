package com.phuonghai.inspection.presentation.navigation

sealed class Screen(val route: String) {
    object SplashScreen : Screen("splash_screen")
    object LoginScreen : Screen("login_screen")
    object OTPScreen : Screen("otp_screen/{verificationId}") {
        fun createRoute(verificationId: String) = "otp_screen/$verificationId"
    }
    object Main : Screen("main")

    // Inspector screens
    object InspectorDashboard : Screen("inspector_dashboard")
    object InspectorNewReportScreen : Screen("inspector_reports")
    object InspectorNotificationScreen : Screen("inspector_notification")
    object InspectorProfileScreen : Screen("inspector_profile")

    // Supervisor screens
    object SupervisorDashboard : Screen("supervisor_dashboard")
    object SupervisorHistoryScreen : Screen("supervisor_history")
    object SupervisorMapScreen : Screen("Supervisor_map")
    object SupervisorProfileScreen: Screen("supervisor_profile")
}