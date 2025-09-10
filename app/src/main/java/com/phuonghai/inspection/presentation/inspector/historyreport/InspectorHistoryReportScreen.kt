package com.phuonghai.inspection.presentation.inspector.historyreport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.phuonghai.inspection.domain.model.Report
import com.phuonghai.inspection.domain.model.SyncStatus
import com.phuonghai.inspection.presentation.theme.SafetyYellow
import androidx.hilt.navigation.compose.hiltViewModel
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
                InspectorHistoryReportCard(report)
            }
        }
    }
}

@Composable
fun InspectorHistoryReportCard(report: Report) {
    val dateFormat = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    val timeString = report.createdAt?.toDate()?.let { dateFormat.format(it) } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(report.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Submitted: $timeString", fontSize = 14.sp, color = Color.Gray)
            Text("Status: ${report.assignStatus}", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            val syncText = if (report.syncStatus == SyncStatus.SYNCED) "Synced" else "UnSynced"
            val syncColor = if (report.syncStatus == SyncStatus.SYNCED) Color(0xFF4CAF50) else SafetyYellow
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .background(syncColor, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(syncText, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
            }
        }
    }
}
