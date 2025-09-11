package com.phuonghai.inspection.presentation.home.supervisor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.phuonghai.inspection.domain.model.Report
import com.phuonghai.inspection.domain.model.User
import com.phuonghai.inspection.presentation.generalUI.ButtonUI
import com.phuonghai.inspection.presentation.navigation.Screen
import com.phuonghai.inspection.presentation.supervisor.dashboard.SupervisorDashboardViewModel
import com.phuonghai.inspection.presentation.supervisor.dashboard.TeamStatistics
import com.phuonghai.inspection.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorDashboardScreen(
    viewModel: SupervisorDashboardViewModel = hiltViewModel(),
    navController: NavController
) {

    val userState by viewModel.user.collectAsState()
    val reportsState by viewModel.reports.collectAsState()
    val statisticState by viewModel.statistic.collectAsState()
    val isLoadingState by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUser()
        viewModel.loadReports()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Text("Supervisor Dashboard", color = Color.White)
                        ButtonUI(
                            text = "Chat",
                            onClick = {
                                navController.navigate(Screen.SupervisorChatBoxScreen.route)
                            },
                            modifier = Modifier.clip(RoundedCornerShape(50.dp))
                        )
                    }},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCharcoal),
            )
        },
        containerColor = DarkCharcoal
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoadingState) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    CircularProgressIndicator(color = SafetyYellow)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 90.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        SupervisorWelcomeSection(user = userState, teamStats = statisticState ?: TeamStatistics())
                    }

                    item {
                        TeamSummarySection(teamStats = statisticState ?: TeamStatistics())
                    }

                    item {
                        PendingReviewsSection(
                            pendingReviews = reportsState,
                            onApprove = {
                                viewModel.approveReport(it)
                            },
                            onReject = {
                                viewModel.rejectReport(it)
                            },
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun SupervisorWelcomeSection(
    user: User? = null,
    teamStats: TeamStatistics
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Hi, ${user?.fullName ?: "Supervisor"}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        val teamMessage = when {
            teamStats == null -> "Loading information..."
            teamStats.totalReports == 0 -> "Not have any report"
            else -> null // we'll build styled text instead
        }

        if (teamMessage != null) {
            Text(
                text = teamMessage,
                fontSize = 17.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            Text(
                text = buildAnnotatedString {
                    append("Today you have ")

                    withStyle(
                        style = SpanStyle(
                            color = SafetyYellow,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(teamStats!!.pendingReviews.toString())
                    }

                    append(" need to consider from team")
                },
                fontSize = 17.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun TeamSummarySection(teamStats: TeamStatistics) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        TeamStatCard("Pending", teamStats.pendingReviews, Color(0xFF2196F3))
        TeamStatCard("Approved", teamStats.approvedReports, Color(0xFF4CAF50))
        TeamStatCard("Rejected", teamStats.rejectedReports, Color(0xFFE53935))
        TeamStatCard("Total", teamStats.totalReports, Color(0xFFFB8C00))
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
    pendingReviews: List<Report>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    navController: NavController
) {
    var showAll by remember { mutableStateOf(false) }
    val reportsToShow = if (showAll) pendingReviews else pendingReviews.take(5)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Report to consider",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (pendingReviews.isEmpty()) {
            Text(
                "Not have any report to consider",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            reportsToShow.forEach { report ->
                PendingReportCard(
                    report = report,
                    onApprove = { onApprove(report.reportId) },
                    onReject = { onReject(report.reportId) },
                    navController = navController
                )
            }

            if (pendingReviews.size > 5) {
                TextButton(
                    onClick = { showAll = !showAll },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (showAll) "Close" else "See more (${pendingReviews.size - 5} another reports)",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun PendingReportCard(
    report: Report,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = {
            navController.navigate(Screen.SupervisorReportDetailScreen.route + "/${report.reportId}")
        },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF333333)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = report.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Description: ${report.description}",
                            fontSize = 16.sp,
                            color = Color.LightGray
                        )
                        Text(
                            text = "Location: ${report.address}",
                            fontSize = 16.sp,
                            color = Color.LightGray
                        )
                        val dateFormat = SimpleDateFormat("EEE MMM dd yyyy", Locale.getDefault())
                        val formattedDate = report.completedAt?.toDate()?.let { dateFormat.format(it) } ?: ""
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween
                        ){
                            Text(
                                text = "Ngày: $formattedDate",
                                fontSize = 16.sp,
                                color = Color.LightGray
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                IconButton(
                                    onClick = onApprove,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Approved",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                IconButton(
                                    onClick = onReject,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Rejected",
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 🔹 Priority Tag
            Text(
                text = report.priority.name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        color = when (report.priority.name) {
                            "HIGH" -> Color(0xFFD32F2F)
                            "NORMAL" -> Color.Gray
                            "LOW" -> Color(0xFF388E3C)
                            else -> Color.Gray
                        },
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Error") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onRetry) {
                    Text("Retry")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        )
    }
}

