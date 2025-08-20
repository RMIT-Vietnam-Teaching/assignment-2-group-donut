package com.example.ui_for_assignment2.ui.screens.HistoryAndSync


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val DarkCharcoal = Color(0xFF1E1E1E)
private val OffWhite = Color(0xFFFAFAFA)
private val SafetyYellow = Color(0xFFFFD700)
private val StatusGreen = Color(0xFF4CAF50)
private val StatusRed = Color(0xFFE53935)
private val StatusOrange = Color(0xFFFB8C00)

@Composable
fun MyReportsScreen() {
    val allReports = listOf(
        ReportHistoryUi("Warehouse A – Safety", "2025-08-15", "Passed", true),
        ReportHistoryUi("Loading Dock – PPE", "2025-08-14", "Failed", false),
        ReportHistoryUi("Fire Exit – Blockage", "2025-08-13", "Needs Attention", false),
        ReportHistoryUi("Equipment Line 2 – QA", "2025-08-12", "Passed", true),
        ReportHistoryUi("Chemical Storage", "2025-08-11", "Failed", true),
        ReportHistoryUi("Emergency Drill", "2025-08-10", "Needs Attention", false)
    )

    var filter by rememberSaveable { mutableStateOf(ReportsFilter.ALL) }

    val filtered = when (filter) {
        ReportsFilter.ALL -> allReports
        ReportsFilter.SYNCED -> allReports.filter { it.synced }
        ReportsFilter.UNSYNCED -> allReports.filter { !it.synced }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Reports") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Filter bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == ReportsFilter.ALL,
                    onClick = { filter = ReportsFilter.ALL },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SafetyYellow,
                        selectedLabelColor = Color.Black
                    )
                )
                FilterChip(
                    selected = filter == ReportsFilter.SYNCED,
                    onClick = { filter = ReportsFilter.SYNCED },
                    label = { Text("Synced") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StatusGreen,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = filter == ReportsFilter.UNSYNCED,
                    onClick = { filter = ReportsFilter.UNSYNCED },
                    label = { Text("Unsynced") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StatusOrange,
                        selectedLabelColor = Color.White
                    )
                )
            }

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filtered) { report ->
                    ReportHistoryCard(report)
                }
            }
        }
    }
}

private enum class ReportsFilter { ALL, SYNCED, UNSYNCED }

private data class ReportHistoryUi(
    val title: String,
    val date: String,
    val status: String, // Passed | Failed | Needs Attention
    val synced: Boolean
)

@Composable
private fun ReportHistoryCard(item: ReportHistoryUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OffWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(
                    text = item.status,
                    color = when (item.status) {
                        "Passed" -> StatusGreen
                        "Failed" -> StatusRed
                        else -> StatusOrange
                    }
                )
            }

            Spacer(Modifier.height(8.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            // Date row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Date",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = item.date,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(12.dp))

            // Sync badge + Export
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val badgeText = if (item.synced) "✅ Synced" else "⚠ Unsynced"
                val badgeColor = if (item.synced) StatusGreen else StatusOrange

                SyncBadge(
                    text = badgeText,
                    color = badgeColor
                )

                OutlinedButton(
                    onClick = { /* placeholder */ },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) {
                    Text("Export PDF")
                }
            }

            if (!item.synced) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Unsynced can be edited",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                // TODO: Provide edit actions when wiring real logic
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(start = 8.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .background(color, shape = MaterialTheme.shapes.small)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SyncBadge(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Preview(name = "My Reports - Light", showBackground = true)
@Composable
private fun PreviewMyReportsLight() {
    MaterialTheme { MyReportsScreen() }
}

@Preview(
    name = "My Reports - Dark",
    showBackground = true,
    backgroundColor = 0xFF1E1E1E
)
@Composable
private fun PreviewMyReportsDark() {
    MaterialTheme { MyReportsScreen() }
}
