package com.phuonghai.inspection.presentation.home.inspector

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectorDashboard(
    viewModel: InspectorDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Sample data for UI testing - replace with real data from ViewModel
    val sampleReports = listOf(
        ReportItem("1", "Fire Safety Report", "PENDING_REVIEW", "2 giờ trước"),
        ReportItem("2", "Elevator Inspection", "NEEDS_ATTENTION", "5 giờ trước"),
        ReportItem("3", "Chemical Storage", "APPROVED", "1 ngày trước"),
        ReportItem("4", "Equipment Check", "DRAFT", "2 ngày trước")
    )

    val sampleStats = DashboardStatistics(
        approvedReports = 5,
        pendingReports = 3,
        draftReports = 4,
        rejectedReports = 1
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", color = MaterialTheme.colorScheme.onSurface) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = { viewModel.refreshDashboard() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                uiState.showError -> {
                    ErrorContent(
                        message = uiState.errorMessage ?: "Có lỗi xảy ra",
                        onRetry = { viewModel.refreshDashboard() },
                        onDismiss = { viewModel.clearError() }
                    )
                }

                else -> {
                    // Use sample data for now, replace with uiState data later
                    DashboardContent(
                        user = uiState.currentUser,
                        statistics = sampleStats,
                        recentReports = sampleReports,
                        isFirstTimeUser = uiState.isEmpty,
                        onCreateReportClick = { viewModel.onCreateReportClick() }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    user: com.phuonghai.inspection.domain.model.User?,
    statistics: DashboardStatistics,
    recentReports: List<ReportItem>,
    isFirstTimeUser: Boolean,
    onCreateReportClick: () -> Unit
) {
    if (isFirstTimeUser) {
        EmptyDashboardView(onCreateReportClick)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummarySection(user, statistics)
            RecentReportsSection(recentReports)

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                onClick = onCreateReportClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tạo báo cáo mới",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text("Tạo báo cáo mới", color = MaterialTheme.colorScheme.onPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SummarySection(
    user: com.phuonghai.inspection.domain.model.User?,
    statistics: DashboardStatistics
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Chào, ${user?.fullName ?: "Inspector"}",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // First row - 3 cards
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReportCountCard("Pending Review", statistics.pendingReports, Color(0xFF2196F3))
                ReportCountCard("Đã duyệt", statistics.approvedReports, Color(0xFF4CAF50))
                ReportCountCard("Từ chối", statistics.rejectedReports, Color(0xFFE53935))
            }

            // Second row - 2 cards (centered)
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Spacer(modifier = Modifier.weight(1f))
                ReportCountCard("Needs Attention", 2, Color(0xFFFFC107)) // Sample data
                ReportCountCard("Nháp", statistics.draftReports, Color(0xFF9E9E9E))
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ReportCountCard(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(text = label, fontSize = 17.sp, color = Color.White)
    }
}

@Composable
fun RecentReportsSection(reports: List<ReportItem>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Báo cáo gần đây",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (reports.isEmpty()) {
            Text("Không có báo cáo gần đây nào.", color = Color.Gray)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(350.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reports) { report ->
                    ReportItemCard(report)
                }
            }
        }
    }
}

@Composable
fun ReportItemCard(report: ReportItem) {
    val safetyYellow = Color(0xFFFFD700)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        report.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = when (report.status) {
                            "PENDING_REVIEW" -> "Pending Review"
                            "APPROVED" -> "Đã duyệt"
                            "REJECTED" -> "Từ chối"
                            "NEEDS_ATTENTION" -> "Needs Attention"
                            "DRAFT" -> "Nháp"
                            else -> report.status
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (report.status) {
                            "PENDING_REVIEW" -> Color(0xFF2196F3)
                            "APPROVED" -> Color(0xFF4CAF50)
                            "REJECTED" -> Color(0xFFE53935)
                            "NEEDS_ATTENTION" -> Color(0xFFFFC107)
                            "DRAFT" -> Color(0xFF9E9E9E)
                            else -> Color.Gray
                        }
                    )
                }

                // Status response tag
                Surface(
                    color = when (report.status) {
                        "APPROVED" -> Color(0x334CAF50) // soft green bg
                        "REJECTED" -> Color(0x33E53935) // soft red bg
                        "PENDING_REVIEW" -> Color(0x332196F3) // soft blue bg
                        else -> Color(0xFF444444) // neutral
                    },
                    shape = RoundedCornerShape(50),
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = when (report.status) {
                            "APPROVED" -> "Approved"
                            "REJECTED" -> "Rejected"
                            "PENDING_REVIEW" -> "Pending"
                            "NEEDS_ATTENTION" -> "Attention"
                            "DRAFT" -> "Draft"
                            else -> "Unknown"
                        },
                        color = when (report.status) {
                            "APPROVED" -> Color(0xFF4CAF50)
                            "REJECTED" -> Color(0xFFE53935)
                            "PENDING_REVIEW" -> Color(0xFF2196F3)
                            "NEEDS_ATTENTION" -> Color(0xFFFFC107)
                            "DRAFT" -> Color(0xFF9E9E9E)
                            else -> Color.LightGray
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("ID: ${report.id}", fontSize = 18.sp, color = Color.Gray)
                    Text("Thời gian: ${report.createdAt}", fontSize = 18.sp, color = Color.Gray)
                }

                // Add notification icon for important reports
                if (report.status == "NEEDS_ATTENTION" || report.status == "REJECTED") {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Cần chú ý",
                        tint = safetyYellow,
                        modifier = Modifier.size(30.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Báo cáo",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyDashboardView(onCreateReportClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = "Empty Reports",
            modifier = Modifier.size(72.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chưa có báo cáo nào",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Bạn chưa có báo cáo nào. Hãy tạo báo cáo đầu tiên để bắt đầu.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCreateReportClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
        ) {
            Text("Tạo báo cáo", color = Color.Black)
        }
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