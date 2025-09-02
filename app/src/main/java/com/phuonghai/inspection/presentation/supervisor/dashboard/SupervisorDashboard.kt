package com.phuonghai.inspection.presentation.home.supervisor

import android.content.res.Resources
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phuonghai.inspection.presentation.supervisor.dashboard.PendingReport
import com.phuonghai.inspection.presentation.supervisor.dashboard.SupervisorDashboardUiState
import com.phuonghai.inspection.presentation.supervisor.dashboard.SupervisorDashboardViewModel
import com.phuonghai.inspection.presentation.supervisor.dashboard.TeamStatistics
import com.phuonghai.inspection.presentation.theme.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorDashboard(
    viewModel: SupervisorDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Supervisor Dashboard", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCharcoal),
                actions = {
                    IconButton(onClick = { viewModel.refreshDashboard() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = DarkCharcoal
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SafetyYellow)
                    }
                }

                uiState.showError -> {
                    ErrorContent(
                        message = uiState.errorMessage ?: "Có lỗi xảy ra",
                        onRetry = { viewModel.refreshDashboard() },
                        onDismiss = { viewModel.clearError() }
                    )
                }

                uiState.showContent -> {
                    SupervisorDashboardContent(
                        uiState = uiState,
                        onViewAllClick = { viewModel.viewAllReports() },
                        onAnalyticsClick = { viewModel.viewAnalytics() },
                        onApproveReport = { reportId -> viewModel.approveReport(reportId) },
                        onRejectReport = { reportId -> viewModel.rejectReport(reportId) }
                    )
                }
            }
        }
    }
}

@Composable
fun SupervisorDashboardContent(
    uiState: SupervisorDashboardUiState,
    onViewAllClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onApproveReport: (String) -> Unit,
    onRejectReport: (String) -> Unit
) {
    val safetyYellow = Color(0xFFFFD700)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SupervisorWelcomeSection(
                user = uiState.currentUser,
                teamStats = uiState.teamStats
            )
        }

        item {
            uiState.teamStats?.let { stats ->
                TeamSummarySection(stats)
            }
        }

        item {
            PendingReviewsSection(
                pendingReviews = uiState.pendingReviews,
                onApprove = onApproveReport,
                onReject = onRejectReport
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    title = "Xem tất cả",
                    icon = Icons.Default.List,
                    color = safetyYellow,
                    onClick = onViewAllClick
                )

                QuickActionButton(
                    title = "Thống kê",
                    icon = Icons.Default.Analytics,
                    color = Color(0xFF2196F3),
                    onClick = onAnalyticsClick
                )
            }
        }
    }
}

@Composable
fun SupervisorWelcomeSection(
    user: com.phuonghai.inspection.domain.model.User?,
    teamStats: TeamStatistics?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Chào, ${user?.fullName ?: "Supervisor"}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            teamStats?.getStatusSummary() ?: "Đang tải thông tin team...",
            fontSize = 16.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
fun TeamSummarySection(teamStats: TeamStatistics) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        TeamStatCard("Chờ duyệt", teamStats.pendingReviews, Color(0xFF2196F3))
        TeamStatCard("Đã duyệt", teamStats.approvedReports, Color(0xFF4CAF50))
        TeamStatCard("Từ chối", teamStats.rejectedReports, Color(0xFFE53935))
        TeamStatCard("Tổng cộng", teamStats.totalReports, Color(0xFFFB8C00))
    }
}

@Composable
fun TeamStatCard(label: String, count: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = count.toString(),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

@Composable
fun PendingReviewsSection(
    pendingReviews: List<PendingReport>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Báo cáo cần duyệt",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (pendingReviews.isEmpty()) {
            Text(
                "Không có báo cáo nào cần duyệt",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            pendingReviews.forEach { report ->
                PendingReportCard(
                    report = report,
                    onApprove = { onApprove(report.id) },
                    onReject = { onReject(report.id) }
                )
            }
        }
    }
}

@Composable
fun PendingReportCard(
    report: PendingReport,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF333333)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = report.title,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Bởi: ${report.inspectorName}",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = report.submittedAt,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Row {
                    IconButton(
                        onClick = onApprove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Duyệt",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onReject,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Từ chối",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .width(120.dp)
            .height(45.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Lỗi") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onRetry) {
                    Text("Thử lại")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Đóng")
                }
            }
        )
    }
}