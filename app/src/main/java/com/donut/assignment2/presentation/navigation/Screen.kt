package com.donut.assignment2.presentation.navigation

sealed class Screen (val route: String){
    object PhoneInputScreen: Screen("phone_input")
    object OTPVerification : Screen("otp_verification/{verificationId}") {
        fun createRoute(verificationId: String) = "otp_verification/$verificationId"
    }
    object Main : Screen("main")
    object InspectorDashboardScreen: Screen("inspector_dashboard")
    object InspectorNewReportScreen: Screen("inspector_new_report")
    object InspectorNotificationScreen: Screen("inspector_notification")
    object InspectorProfileScreen: Screen("inspector_profile")

    // Supervisor screens
    object SupervisorDashboardScreen : Screen("supervisor_dashboard")
    object SupervisorHistoryScreen : Screen("supervisor_history")
    object SupervisorMapScreen : Screen("supervisor_map")
    object SupervisorProfileScreen : Screen("supervisor_profile")
}