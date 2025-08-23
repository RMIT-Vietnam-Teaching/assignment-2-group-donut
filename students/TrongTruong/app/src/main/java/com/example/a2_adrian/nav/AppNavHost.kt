package com.example.a2_adrian.nav


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.a2_adrian.data.FakeRepository
import com.example.a2_adrian.ui.screens.NewReportScreen
import com.example.a2_adrian.ui.screens.NotificationsScreen
import com.example.a2_adrian.ui.screens.ReportDetailScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.NewReport.route
    ) {
        composable(Screen.NewReport.route) {
            NewReportScreen(
                onSubmit = {
                    // demo: mở chi tiết report đầu tiên
                    navController.navigate(Screen.ReportDetail.route("A-102"))
                },
                onOpenNotifications = {
                    navController.navigate(Screen.Notifications.route)
                }
            )
        }
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onOpenDetail = { id ->
                    navController.navigate(Screen.ReportDetail.route(id))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ReportDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("reportId") ?: "A-102"
            val report = FakeRepository.getById(id) ?: FakeRepository.reports.first()
            ReportDetailScreen(
                report = report,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
