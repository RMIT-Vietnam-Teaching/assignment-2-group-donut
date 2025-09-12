package com.phuonghai.inspection.presentation.inspector.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.phuonghai.inspection.presentation.theme.OffWhite
import com.phuonghai.inspection.presentation.theme.SafetyYellow
import com.phuonghai.inspection.presentation.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectorReportDetailScreen(
    navController: NavController,
    viewModel: InspectorReportDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "An unknown error occurred.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                uiState.fullReport != null -> {
                    ReportDetails(uiState)
                }
            }
        }
    }
}

@Composable
private fun ReportDetails(uiState: ReportDetailUiState) {
    val report = uiState.fullReport!! // We know it's not null here
    val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy, HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp) // Tăng khoảng cách
    ) {
        // === THÔNG TIN MỚI ===
        DetailRow("Report Title", uiState.reportTitle)
        DetailRow("Inspector", uiState.inspectorName)
        DetailRow("Task", uiState.taskTitle)
        Divider()

        // === HIỂN THỊ MEDIA ===
        if (uiState.imageUrl.isNotBlank() || uiState.videoUrl.isNotBlank()) {
            Text("Media Attachments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SafetyYellow)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (uiState.imageUrl.isNotBlank()) {
            AsyncImage(
                model = uiState.imageUrl,
                contentDescription = "Attached image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
        if (uiState.videoUrl.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(Icons.Default.Videocam, contentDescription = "Video Icon", tint = TextSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Video attached (viewing not supported)", color = TextSecondary)
            }
        }
        if (uiState.imageUrl.isNotBlank() || uiState.videoUrl.isNotBlank()) {
            Divider()
        }

        // === CÁC THÔNG TIN KHÁC ===
        DetailRow("Type", report.type.name)
        DetailRow("Address", report.address.ifEmpty { "N/A" })
        DetailRow("Created At", report.createdAt?.toDate()?.let { dateFormat.format(it) })
        DetailRow("Completed At", report.completedAt?.toDate()?.let { dateFormat.format(it) })
        Divider()
        DetailRow("Assign Status", report.assignStatus.name)
        DetailRow("Response Status", report.responseStatus.name)
        DetailRow("Sync Status", report.syncStatus.name)
        Divider()
        DetailRow("Description", report.description)
        DetailRow("Score", report.score?.toString() ?: "N/A")
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String?) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            fontSize = 14.sp
        )
        Text(
            text = value ?: "N/A",
            style = MaterialTheme.typography.bodyLarge,
            color = OffWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}