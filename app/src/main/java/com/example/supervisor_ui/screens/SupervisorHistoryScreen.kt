package com.example.supervisor_ui.screens


import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.example.supervisor_ui.data.InspectionStatus
import com.example.supervisor_ui.data.InspectionAction

@Composable
fun SupervisorHistoryScreen(navController: NavHostController) {
    SupervisorInspectionListScreen(
        navController = navController,
        filterPredicate = { it.action != InspectionAction.NONE },
        statusDisplayMap = mapOf(
            InspectionStatus.PENDING_REVIEW to "Pending Review",
            InspectionStatus.PASSED to "Passed",
            InspectionStatus.FAILED to "Failed",
            InspectionStatus.NEEDS_ATTENTION to "Needs Attention"
        )
    )
}