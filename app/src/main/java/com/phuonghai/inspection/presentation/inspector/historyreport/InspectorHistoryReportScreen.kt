package com.phuonghai.inspection.presentation.inspector.historyreport

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // THÊM IMPORT NÀY
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.phuonghai.inspection.domain.model.Report
import com.phuonghai.inspection.domain.model.SyncStatus
import com.phuonghai.inspection.presentation.theme.SafetyYellow
import androidx.hilt.navigation.compose.hiltViewModel
import com.phuonghai.inspection.domain.model.ResponseStatus
import com.phuonghai.inspection.presentation.navigation.Screen
import com.phuonghai.inspection.presentation.theme.SafetyRed
import com.phuonghai.inspection.presentation.theme.StatusGreen
import com.phuonghai.inspection.presentation.theme.StatusOrange
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectorHistoryReportScreen(navController: NavController) {
    val viewModel: InspectorHistoryViewModel = hiltViewModel()
    val reports by viewModel.reports.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("History") })
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reports) { report ->
                InspectorHistoryReportCard(
                    report = report,
                    onClick = {
                        // Điều hướng đến màn hình chi tiết khi nhấn vào card
                        navController.navigate(Screen.InspectorReportDetailScreen.createRoute(report.reportId))
                    }
                )
            }
        }
    }
}

@Composable
fun InspectorHistoryReportCard(report: Report, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    val timeString = report.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "N/A"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)), // Màu nền card
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp) // Tăng padding tổng thể để có không gian
        ) {
            // Tiêu đề báo cáo
            Text(
                report.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1, // Giới hạn 1 dòng cho tiêu đề
                overflow = TextOverflow.Ellipsis // Thêm dấu ... nếu quá dài
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Thời gian gửi
            Text(
                "Submitted: $timeString",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp)) // Khoảng cách lớn hơn

            // Hàng chứa Status và Review Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Căn đều 2 đầu
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status của Report (assignStatus)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.TaskAlt, // Biểu tượng cho trạng thái nhiệm vụ
                        contentDescription = "Task Status",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Task: ${report.assignStatus}",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Normal
                    )
                }


                // Response Status (Review)
                val responseStatusText = report.responseStatus.name
                val responseStatusColor = when (report.responseStatus) {
                    ResponseStatus.APPROVED -> StatusGreen
                    ResponseStatus.REJECTED -> SafetyRed
                    ResponseStatus.PENDING -> StatusOrange
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when (report.responseStatus) {
                            ResponseStatus.APPROVED -> Icons.Default.CheckCircle // Biểu tượng Approved
                            ResponseStatus.REJECTED -> Icons.Default.Cancel // Biểu tượng Rejected
                            ResponseStatus.PENDING -> Icons.Default.HourglassEmpty // Biểu tượng Pending
                        },
                        contentDescription = "Review Status",
                        tint = responseStatusColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Review: $responseStatusText",
                        fontSize = 14.sp,
                        color = responseStatusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Sync Status (căn phải)
            val syncText = if (report.syncStatus == SyncStatus.SYNCED) "Synced" else "Unsynced"
            val syncColor = if (report.syncStatus == SyncStatus.SYNCED) Color(0xFF4CAF50) else SafetyYellow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.End) // Căn phải cho Box chứa Sync Status
            ) {
                Row(
                    modifier = Modifier
                        .background(syncColor, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .align(Alignment.CenterEnd), // Căn nội dung của Row vào cuối Box
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        if (report.syncStatus == SyncStatus.SYNCED) Icons.Default.CloudDone else Icons.Default.CloudOff,
                        contentDescription = "Sync Status",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        syncText,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}