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

    object InspectorTaskScreen : Screen("inspector_task")

    object InspectorHistoryScreen : Screen("inspector_history")
    object InspectorNewReportScreen : Screen("inspector_reports")
    object InspectorNotificationScreen : Screen("inspector_notification")
    object InspectorProfileScreen : Screen("inspector_profile")
    object InspectorChatDetailScreen: Screen("inspector_chat_detail")

    // Supervisor screens
    object SupervisorDashboard : Screen("supervisor_dashboard")
    object SupervisorTaskScreen : Screen("supervisor_task")
    object SupervisorHistoryScreen : Screen("supervisor_history")
    object SupervisorMapScreen : Screen("Supervisor_map")
    object SupervisorProfileScreen: Screen("supervisor_profile")

    object SupervisorReportDetailScreen : Screen("supervisor_report_detail")
    object SupervisorChatBoxScreen: Screen("supervisor_chat_box")
    object SupervisorChatDetailScreen: Screen("supervisor_chat_detail")

}