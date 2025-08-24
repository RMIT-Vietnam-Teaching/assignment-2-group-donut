package com.donut.assignment2.presentation.inspector.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.donut.assignment2.domain.model.InspectorDashboard
import com.donut.assignment2.domain.model.Report
import com.donut.assignment2.domain.model.ReportStatus
import com.donut.assignment2.presentation.inpsector.dashboard.InspectorDashboardViewModel
import com.donut.assignment2.presentation.theme.SafetyYellow
import com.donut.assignment2.presentation.theme.DarkCharcoal
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectorDashboardScreen(
    userId: String,
    navController: NavController,
    viewModel: InspectorDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isRefreshing)

    LaunchedEffect(userId) {
        viewModel.loadDashboard(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCharcoal)
            )
        },
        containerColor = DarkCharcoal
    ) { innerPadding ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refreshDashboard(userId) },
            modifier = Modifier.padding(innerPadding)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.showLoading) {
                    CircularProgressIndicator(color = SafetyYellow)
                }

                uiState.errorMessage?.let { error ->
                    AlertDialog(
                        onDismissRequest = { viewModel.clearError() },
                        title = { Text("Lỗi") },
                        text = { Text(error) },
                        confirmButton = {
                            TextButton(onClick = { viewModel.retryLoad(userId) }) {
                                Text("Thử lại")
                            }
                        }
                    )
                }

                uiState.dashboard?.let { dashboard ->
                    if (dashboard.isFirstTimeUser) {
                        EmptyDashboardView(navController)
                    } else {
                        DashboardContent(dashboard, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardContent(dashboard: InspectorDashboard, navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Hiển thị thông tin tổng hợp
            SummarySection(dashboard)
        }

        item {
            // Section các báo cáo gần đây
            RecentReportsSection(dashboard.recentReports)
        }

        item {
            // Nút tạo báo cáo mới
            Button(
                modifier = Modifier.fillMaxWidth().height(50.dp),
                onClick = {
                    navController.navigate("create_report/${dashboard.user.phoneNumber}")
                },
                colors = ButtonDefaults.buttonColors(containerColor = SafetyYellow),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tạo báo cáo mới"
                )
                Spacer(Modifier.width(8.dp))
                Text("Tạo báo cáo mới", color = DarkCharcoal)
            }
        }
    }
}

@Composable
fun SummarySection(dashboard: InspectorDashboard) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Chào, ${dashboard.user.fullName}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            dashboard.getStatusSummary(),
            fontSize = 16.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ReportCountCard("Đã duyệt", dashboard.approvedReports, Color(0xFF4CAF50))
            ReportCountCard("Đang duyệt", dashboard.inReview, Color(0xFF2196F3))
            ReportCountCard("Nháp", dashboard.draftReports, Color(0xFFFB8C00))
            ReportCountCard("Từ chối", dashboard.rejectedReports, Color(0xFFE53935))
        }
    }
}

@Composable
fun ReportCountCard(label: String, count: Int, color: Color) {
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
fun RecentReportsSection(reports: List<Report>) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Báo cáo gần đây",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (reports.isEmpty()) {
            Text(
                "Không có báo cáo gần đây nào.",
                color = Color.Gray
            )
        } else {
            reports.forEach { report ->
                ReportItemCard(report)
            }
        }
    }
}

@Composable
fun ReportItemCard(report: Report) {
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
            Text(
                text = "Trạng thái: ${report.status.name}",
                fontSize = 12.sp,
                color = when (report.status) {
                    ReportStatus.DRAFT -> Color(0xFFFB8C00)
                    ReportStatus.SUBMITTED, ReportStatus.UNDER_REVIEW -> Color(0xFF2196F3)
                    ReportStatus.APPROVED -> Color(0xFF4CAF50)
                    ReportStatus.REJECTED -> Color(0xFFE53935)
                }
            )
        }
    }
}

@Composable
fun EmptyDashboardView(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Chào mừng bạn đến với ứng dụng!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Hãy bắt đầu bằng việc tạo báo cáo đầu tiên của bạn.",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            onClick = { navController.navigate("create_report/userId_placeholder") },
            colors = ButtonDefaults.buttonColors(containerColor = SafetyYellow),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Tạo Báo Cáo Mới", color = DarkCharcoal)
        }
    }
}