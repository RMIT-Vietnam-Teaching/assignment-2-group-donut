package com.example.ui_for_assignment2.ui.screens.coreFlow


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

private val DarkCharcoal = Color(0xFF1E1E1E)
private val OffWhite = Color(0xFFFAFAFA)
private val SafetyYellow = Color(0xFFFFD700)
private val StatusGreen = Color(0xFF4CAF50)
private val StatusRed = Color(0xFFE53935)
private val StatusOrange = Color(0xFFFB8C00)

@Composable
fun HomeScreen() {
    val reports = listOf(
        ReportUi("Warehouse A Inspection", "Passed", "2025-08-15"),
        ReportUi("Loading Dock Safety", "Failed", "2025-08-14"),
        ReportUi("Fire Exit Audit", "Needs Attention", "2025-08-13"),
        ReportUi("Equipment Check - Line 2", "Passed", "2025-08-12"),
        ReportUi("PPE Compliance Review", "Passed", "2025-08-11"),
        ReportUi("Chemical Storage", "Failed", "2025-08-10"),
        ReportUi("Emergency Drill Review", "Needs Attention", "2025-08-09"),
        ReportUi("Generator Room", "Passed", "2025-08-08"),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                actions = {
                    IconButton(onClick = { /* TODO: placeholder only */ }) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text("3")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    }
                }
            )
        }
        // TODO: Bottom Navigation can be placed in Scaffold(bottomBar = { ... })
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            StatsCard(
                total = 128,
                passed = 84,
                failed = 22,
                needsAttention = 22
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Recent Reports",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(reports) { report ->
                    ReportCard(report)
                }
            }
        }
    }
}

private data class ReportUi(
    val title: String,
    val status: String,
    val date: String
)

@Composable
private fun StatsCard(
    total: Int,
    passed: Int,
    failed: Int,
    needsAttention: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OffWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Total", value = total.toString(), accent = SafetyYellow)
                StatItem(label = "Passed", value = passed.toString(), accent = StatusGreen)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Failed", value = failed.toString(), accent = StatusRed)
                StatItem(
                    label = "Needs Attention",
                    value = needsAttention.toString(),
                    accent = StatusOrange
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, accent: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(50))
                .background(accent)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ReportCard(report: ReportUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusChip(
                    text = report.status,
                    color = when (report.status) {
                        "Passed" -> StatusGreen
                        "Failed" -> StatusRed
                        else -> StatusOrange
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

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
                    text = report.date,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Preview(name = "Home - Light", showBackground = true)
@Composable
private fun PreviewHomeLight() {
    MaterialTheme { HomeScreen() }
}

@Preview(
    name = "Home - Dark",
    showBackground = true,
    backgroundColor = 0xFF1E1E1E
)
@Composable
private fun PreviewHomeDark() {
    // Dark preview uses same UI; background is forced dark for placeholder
    MaterialTheme { HomeScreen() }
}
