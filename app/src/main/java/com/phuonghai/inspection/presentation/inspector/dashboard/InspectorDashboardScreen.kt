package com.phuonghai.inspection.presentation.home.inspector

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.phuonghai.inspection.domain.model.User
import com.phuonghai.inspection.presentation.generalUI.ButtonUI
import com.phuonghai.inspection.presentation.navigation.Screen
import com.phuonghai.inspection.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Theme colors - add these if not already defined in your theme files
val StatusOrange = Color(0xFFFF9800)
val StatusBlue = Color(0xFF2196F3)
val StatusGreen = Color(0xFF4CAF50)
val StatusGray = Color(0xFF9E9E9E)
val SafetyRed = Color(0xFFF44336)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectorDashboardScreen(
    viewModel: InspectorDashboardViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                    ){
                        Text(
                            "Dashboard",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        ButtonUI(
                            text = "Chat",
                            onClick = {
                                navController.navigate(Screen.InspectorChatDetailScreen.route)
                            },
                            modifier = Modifier.clip(RoundedCornerShape(50.dp))
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                    DashboardErrorContent(
                        message = uiState.errorMessage ?: "Đã có lỗi xảy ra",
                        onRetry = { viewModel.refreshDashboard() }
                    )
                }

                uiState.showContent -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Welcome Header
                        item {
                            WelcomeHeader(user = uiState.currentUser)
                        }

                        // Statistics Overview
                        item {
                            StatisticsOverview(
                                pendingReports = uiState.pendingReports.size,
                                todayTasks = uiState.todayTasks.size,
                                totalReports = uiState.statistics?.getTotalCount() ?: 0,
                                completedTasks = uiState.statistics?.approvedReports ?: 0
                            )
                        }

                        // Pending Reports Section
                        item {
                            SectionHeader(
                                title = "Báo cáo chờ duyệt",
                                count = uiState.pendingReports.size,
                                icon = Icons.Default.PendingActions,
                                color = StatusOrange
                            )
                        }

                        if (uiState.pendingReports.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    message = "Không có báo cáo nào đang chờ duyệt",
                                    icon = Icons.Default.CheckCircle
                                )
                            }
                        } else {
                            items(uiState.pendingReports) { report ->
                                PendingReportCard(
                                    report = report,
                                    onClick = {
                                        // Navigate to report detail
                                        navController.navigate("report_detail/${report.id}")
                                    }
                                )
                            }
                        }

                        // Today Tasks Section
                        item {
                            SectionHeader(
                                title = "Nhiệm vụ hôm nay",
                                count = uiState.todayTasks.size,
                                icon = Icons.Default.Today,
                                color = StatusBlue
                            )
                        }

                        if (uiState.todayTasks.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    message = "Không có nhiệm vụ nào cho hôm nay",
                                    icon = Icons.Default.EventAvailable
                                )
                            }
                        } else {
                            items(uiState.todayTasks) { task ->
                                TaskCard(
                                    task = task,
                                    onClick = {
                                        // Navigate to task detail or create report
                                        navController.navigate("task_detail/${task.taskId}")
                                    }
                                )
                            }
                        }

                        // Quick Actions
                        item {
                            QuickActions(
                                onCreateReport = {
                                    navController.navigate(Screen.InspectorNewReportScreen.route)
                                },
                                onViewAllTasks = {
                                    navController.navigate(Screen.InspectorTaskScreen.route)
                                },
                                onViewHistory = {
                                    navController.navigate(Screen.InspectorHistoryScreen.route)
                                }
                            )
                        }
                    }
                }

                else -> {
                    // Initial state or empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeHeader(user: User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Inspector Info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Xin chào, ${user?.fullName ?: "Inspector"}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "ID: INSP-${user?.uId?.take(6)?.uppercase() ?: "000000"}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Hôm nay là ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatisticsOverview(
    pendingReports: Int,
    todayTasks: Int,
    totalReports: Int,
    completedTasks: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tổng quan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    number = pendingReports,
                    label = "Chờ duyệt",
                    color = StatusOrange,
                    icon = Icons.Default.PendingActions
                )

                StatCard(
                    number = todayTasks,
                    label = "Task hôm nay",
                    color = StatusBlue,
                    icon = Icons.Default.Today
                )

                StatCard(
                    number = totalReports,
                    label = "Tổng báo cáo",
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Default.Assignment
                )

                StatCard(
                    number = completedTasks,
                    label = "Đã hoàn thành",
                    color = StatusGreen,
                    icon = Icons.Default.CheckCircle
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    number: Int,
    label: String,
    color: Color,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = number.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )

        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                color = color,
                shape = RoundedCornerShape(50),
                tonalElevation = 2.dp
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun PendingReportCard(
    report: ReportItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = report.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Gửi lúc: ${report.createdAt}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = StatusOrange.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50),
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = "⏳ Chờ duyệt",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = StatusOrange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = task.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Hạn: ${task.dueTime}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = when (task.priority) {
                        "HIGH" -> SafetyRed.copy(alpha = 0.2f)
                        "NORMAL" -> StatusBlue.copy(alpha = 0.2f)
                        else -> StatusGray.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(50),
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = when (task.priority) {
                            "HIGH" -> "🔴 Cao"
                            "NORMAL" -> "🔵 Bình thường"
                            else -> "⚪ Thấp"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (task.priority) {
                            "HIGH" -> SafetyRed
                            "NORMAL" -> StatusBlue
                            else -> StatusGray
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View task",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    message: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = message,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuickActions(
    onCreateReport: () -> Unit,
    onViewAllTasks: () -> Unit,
    onViewHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Thao tác nhanh",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Default.Add,
                    label = "Tạo báo cáo",
                    onClick = onCreateReport
                )

                QuickActionButton(
                    icon = Icons.Default.Assignment,
                    label = "Xem tất cả task",
                    onClick = onViewAllTasks
                )

                QuickActionButton(
                    icon = Icons.Default.History,
                    label = "Lịch sử",
                    onClick = onViewHistory
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 4.dp,
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun DashboardErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = SafetyRed,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        ButtonUI(
            text = "Thử lại",
            onClick = onRetry
        )
    }
}