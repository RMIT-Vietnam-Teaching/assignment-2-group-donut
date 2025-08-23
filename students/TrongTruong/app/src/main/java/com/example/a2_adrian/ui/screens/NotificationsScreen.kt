package com.example.a2_adrian.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a2_adrian.data.*
import com.example.a2_adrian.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onOpenDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    var filter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Approved", "Rejected", "Pending")

    val rows = FakeRepository.reports.map { r ->
        when (r.status) {
            ReportStatus.PASSED -> "Approved" to r
            ReportStatus.FAILED -> "Rejected" to r
            ReportStatus.NEEDS  -> "Pending"  to r
        }
    }.filter { (tag, _) -> filter == "All" || filter == tag }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark),
                navigationIcon = { TextButton(onClick = onBack) { Text("←", fontSize = 30.sp, color = OffWhite) } }
            )
        }
    ) { inner ->
        Column(
            Modifier.padding(inner).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filters.forEach { f ->
                    val c = when (f) {
                        "Approved" -> StatusGreen
                        "Rejected" -> StatusRed
                        "Pending"  -> StatusOrange
                        else       -> SafetyYellow
                    }
                    val sel = filter == f
                    FilterChip(
                        selected = sel,
                        onClick = { filter = f },
                        label = { Text(f) },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = c,
                            selectedLabelColor = if (f == "All") Color.Black else Color.White,
                            containerColor = SurfaceDarkHigh,
                            labelColor = if (f == "All") OffWhite else TextSecondary
                        ),
                        border = if (sel) null else BorderStroke(1.dp, if (f == "All") BorderDark else c)
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(rows) { (tag, report) ->
                    ElevatedCard(
                        onClick = { onOpenDetail(report.id) },
                        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceDarkHigh)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val icon = when (tag) { "Approved" -> "✅"; "Rejected" -> "❌"; else -> "⏳" }
                            Text("$icon  ${report.title}", fontWeight = FontWeight.SemiBold, color = OffWhite)
                            // chip trạng thái trong list
                            AssistChip(
                                onClick = {}, shape = CircleShape,
                                label = { Text(tag) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = when (tag) {
                                        "Approved" -> StatusGreen
                                        "Rejected" -> StatusRed
                                        else       -> StatusOrange
                                    },
                                    labelColor = Color.White
                                )
                            )
                            Text("Today • 10:45", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}
