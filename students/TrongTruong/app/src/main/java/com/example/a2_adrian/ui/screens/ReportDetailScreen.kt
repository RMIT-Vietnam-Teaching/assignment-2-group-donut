package com.example.a2_adrian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a2_adrian.data.Report
import com.example.a2_adrian.data.ReportStatus
import com.example.a2_adrian.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    report: Report,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = { Text("New inspection") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark),
                navigationIcon = { TextButton(onClick = onBack) { Text("←", fontSize = 30.sp, color = OffWhite) } },
                actions = { TextButton(onClick = { /* ... */ }) { Text("⋯", fontSize = 30.sp, color = OffWhite) } }
            )
        }
    ) { inner ->
        Column(
            Modifier.padding(inner).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(report.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = OffWhite)

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val c = when (report.status) {
                    ReportStatus.PASSED -> StatusGreen
                    ReportStatus.FAILED -> StatusRed
                    ReportStatus.NEEDS  -> StatusOrange
                }
                AssistChip(
                    onClick = {}, shape = CircleShape,
                    label = { Text(statusLabel(report.status)) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = c, labelColor = Color.White)
                )
                Text(report.date, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }

            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = SurfaceDarkHigh)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Inspection Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = OffWhite)
                    KeyValueRow("Inspection Type", report.inspectionType)
                    KeyValueRow("Inspector", report.inspector)
                    KeyValueRow("Score", report.score.toString())
                    KeyValueRow("Outcome", report.outcome)
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Sync", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        val badgeColor = if (report.synced) StatusGreen else StatusOrange
                        val badgeBg = if (report.synced) Color(0xFF163D2A) else Color(0xFF3E2A13) // rất nhạt trên dark
                        AssistChip(
                            onClick = {}, shape = CircleShape,
                            label = { Text(if (report.synced) "✅ Synced" else "⚠ Unsynced") },
                            colors = AssistChipDefaults.assistChipColors(containerColor = badgeBg, labelColor = badgeColor)
                        )
                    }
                }
            }

            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = SurfaceDarkHigh)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = OffWhite)
                    Spacer(Modifier.height(8.dp))
                    Text(report.notes, color = OffWhite)
                }
            }

            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = SurfaceDarkHigh)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Attachments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = OffWhite)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(3) {
                            Box(
                                Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceDark),
                                contentAlignment = Alignment.Center
                            ) { Text("📷") }
                        }
                    }
                }
            }

            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = SurfaceDarkHigh)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = OffWhite)
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BorderDark),
                        contentAlignment = Alignment.Center
                    ) { Text("Map placeholder / GPS ${report.latitude}, ${report.longitude}", color = OffWhite) }
                }
            }
        }
    }
}

@Composable
private fun KeyValueRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = OffWhite)
    }
}

private fun statusLabel(s: ReportStatus) = when (s) {
    ReportStatus.PASSED -> "Passed"
    ReportStatus.FAILED -> "Failed"
    ReportStatus.NEEDS  -> "Needs Attention"
}
