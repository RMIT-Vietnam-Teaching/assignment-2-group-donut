package com.phuonghai.inspection.presentation.home.inspector

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phuonghai.inspection.presentation.theme.*

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
        containerColor = MaterialTheme.colorScheme.background, // ✅ Use theme background
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dashboard",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                },
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SummarySection(user, statistics)
            }

            item {
                RecentReportsSection(recentReports)
            }

            item {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    onClick = onCreateReportClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tạo báo cáo mới",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Tạo báo cáo mới",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SummarySection(
    user: com.phuonghai.inspection.domain.model.User?,
    statistics: DashboardStatistics
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Chào, ${user?.fullName ?: "Inspector"}",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Statistics cards with your color scheme
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // First row - 3 cards
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                ReportCountCard(
                    "Pending\nReview",
                    statistics.pendingReports,
                    StatusBlue,
                    modifier = Modifier.weight(1f)
                )
                ReportCountCard(
                    "Đã\nduyệt",
                    statistics.approvedReports,
                    StatusGreen, // ✅ Using your StatusGreen
                    modifier = Modifier.weight(1f)
                )
                ReportCountCard(
                    "Từ\nchối",
                    statistics.rejectedReports,
                    SafetyRed, // ✅ Using your SafetyRed
                    modifier = Modifier.weight(1f)
                )
            }

            // Second row - 2 cards (centered)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReportCountCard(
                    "Needs\nAttention",
                    2,
                    StatusOrange, // ✅ Using your StatusOrange
                    modifier = Modifier.width(120.dp)
                )
                ReportCountCard(
                    "Nháp",
                    statistics.draftReports,
                    StatusGray, // ✅ Using your StatusGray
                    modifier = Modifier.width(120.dp)
                )
            }
        }
    }
}

@Composable
fun ReportCountCard(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun RecentReportsSection(reports: List<ReportItem>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Báo cáo gần đây",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (reports.isEmpty()) {
            Text(
                "Không có báo cáo gần đây nào.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                reports.forEach { report ->
                    ReportItemCard(report)
                }
            }
        }
    }
}

@Composable
fun ReportItemCard(report: ReportItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        report.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
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
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (report.status) {
                            "PENDING_REVIEW" -> StatusBlue
                            "APPROVED" -> StatusGreen
                            "REJECTED" -> SafetyRed
                            "NEEDS_ATTENTION" -> StatusOrange
                            "DRAFT" -> StatusGray
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // Status badge
                Surface(
                    color = when (report.status) {
                        "APPROVED" -> StatusGreen.copy(alpha = 0.2f)
                        "REJECTED" -> SafetyRed.copy(alpha = 0.2f)
                        "PENDING_REVIEW" -> StatusBlue.copy(alpha = 0.2f)
                        "NEEDS_ATTENTION" -> StatusOrange.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(50),
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = when (report.status) {
                            "APPROVED" -> "✓"
                            "REJECTED" -> "✗"
                            "PENDING_REVIEW" -> "⏳"
                            "NEEDS_ATTENTION" -> "⚠"
                            "DRAFT" -> "📝"
                            else -> "•"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (report.status) {
                            "APPROVED" -> StatusGreen
                            "REJECTED" -> SafetyRed
                            "PENDING_REVIEW" -> StatusBlue
                            "NEEDS_ATTENTION" -> StatusOrange
                            "DRAFT" -> StatusGray
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "ID: ${report.id}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${report.createdAt}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyDashboardView(onCreateReportClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = "Empty Reports",
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chưa có báo cáo nào",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Bạn chưa có báo cáo nào. Hãy tạo báo cáo đầu tiên để bắt đầu.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCreateReportClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                "Tạo báo cáo",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
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
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    "Lỗi",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    message,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(onClick = onRetry) {
                    Text(
                        "Thử lại",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        "Đóng",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}