package com.example.a2_adrian.nav


sealed class Screen(val route: String) {
    data object NewReport : Screen("new_report")
    data object Notifications : Screen("notifications")
    data object ReportDetail : Screen("report_detail/{reportId}") {
        fun route(reportId: String) = "report_detail/$reportId"
    }
}
