package com.donut.assignment2.presentation.supervisor.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.phuonghai.inspection.presentation.theme.SafetyYellow
import java.text.SimpleDateFormat
import java.util.*

// ---- Fake Models for Testing UI ----
data class FakeUser(val id: String, val fullName: String)

// ---- Fake Models ----
enum class FakeAssignStatus { PENDING_REVIEW, PASSED, FAILED, NEEDS_ATTENTION, DRAFT }
enum class FakeSupervisorResponse { APPROVED, REJECTED, NONE }

data class FakeReport(
    val id: String,
    val title: String,
    val assignStatus: FakeAssignStatus,        // inspector’s assignment
    val supervisorResponse: FakeSupervisorResponse, // supervisor’s response
    val supervisorName: String,
    val inspectorName: String,
    val time: Long,
    val hasNote: Boolean = false
)

data class FakeDashboard(
    val user: FakeUser,
    val pendingReports: Int,
    val passedReports: Int,
    val failedReports: Int,
    val needsAttention: Int,
    val draftReports: Int,
    val recentReports: List<FakeReport>,
    val isFirstTimeUser: Boolean = false
) {
    fun getStatusSummary(): String {
        return "Pending: $pendingReports • Passed: $passedReports • Failed: $failedReports • Needs Attention: $needsAttention • Draft: $draftReports"
    }
}

// ✅ Fake reports for supervisor dashboard
val reports = listOf(
    FakeReport(
        id = "R001",
        title = "Unsafe Wiring Found",
        supervisorName = "Supervisor A",
        inspectorName = "Inspector A",
        time = System.currentTimeMillis() - 1000 * 60 * 60, // 1h ago
        assignStatus = FakeAssignStatus.PENDING_REVIEW,
        supervisorResponse = FakeSupervisorResponse.NONE,
        hasNote = true
    ),
    FakeReport(
        id = "R002",
        title = "Fire Extinguisher Missing",
        supervisorName = "Supervisor B",
        inspectorName = "Inspector B",
        time = System.currentTimeMillis() - 1000 * 60 * 60 * 24, // 1 day ago
        assignStatus = FakeAssignStatus.PASSED,
        supervisorResponse = FakeSupervisorResponse.APPROVED
    ),
    FakeReport(
        id = "R003",
        title = "Blocked Emergency Exit",
        supervisorName = "Supervisor C",
        inspectorName = "Inspector C",
        time = System.currentTimeMillis() - 1000 * 60 * 60 * 48, // 2 days ago
        assignStatus = FakeAssignStatus.PENDING_REVIEW,
        supervisorResponse = FakeSupervisorResponse.NONE
    ),
    FakeReport(
        id = "R004",
        title = "Broken Sprinkler System",
        supervisorName = "Supervisor D",
        inspectorName = "Inspector D",
        time = System.currentTimeMillis() - 1000 * 60 * 30, // 30 min ago
        assignStatus = FakeAssignStatus.NEEDS_ATTENTION,
        supervisorResponse = FakeSupervisorResponse.APPROVED,
        hasNote = true
    ),
    FakeReport(
        id = "R005",
        title = "Expired Safety Signs",
        supervisorName = "Supervisor E",
        inspectorName = "Inspector E",
        time = System.currentTimeMillis() - 1000 * 60 * 60 * 5, // 5h ago
        assignStatus = FakeAssignStatus.FAILED,
        supervisorResponse = FakeSupervisorResponse.REJECTED
    ),
    FakeReport(
        id = "R006",
        title = "Emergency Drill Not Conducted",
        supervisorName = "Supervisor F",
        inspectorName = "Inspector F",
        time = System.currentTimeMillis() - 1000 * 60 * 60 * 72, // 3 days ago
        assignStatus = FakeAssignStatus.PASSED,
        supervisorResponse = FakeSupervisorResponse.NONE
    ),
    FakeReport(
        id = "R007",
        title = "First Aid Kit Missing",
        supervisorName = "Supervisor G",
        inspectorName = "Inspector G",
        time = System.currentTimeMillis() - 1000 * 60 * 60 * 96, // 4 days ago
        assignStatus = FakeAssignStatus.FAILED,
        supervisorResponse = FakeSupervisorResponse.NONE // edge case
    ),
    FakeReport(
        id = "R008",
        title = "Obstructed Fire Hose",
        supervisorName = "Supervisor H",
        inspectorName = "Inspector H",
        time = System.currentTimeMillis() - 1000 * 60 * 60 * 120, // 5 days ago
        assignStatus = FakeAssignStatus.PASSED,
        supervisorResponse = FakeSupervisorResponse.NONE
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorHistoryScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }

    // Dropdown states
    var expandedTime by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }
    var expandedResponse by remember { mutableStateOf(false) }

    // Selected filters
    var selectedTimeFilter by remember { mutableStateOf("Most Recent") }
    var selectedStatusFilter by remember { mutableStateOf("All Status") }
    var selectedResponseFilter by remember { mutableStateOf("All Responses") }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E))
                    .padding(12.dp, top = 60.dp)
            ) {
                // 🔍 Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search reports...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2C2C2C), RoundedCornerShape(8.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SafetyYellow,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = SafetyYellow
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 🎛 Filter dropdown chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Time filter
                    Box {
                        FilterChip(
                            selected = expandedTime,
                            onClick = { expandedTime = !expandedTime },
                            label = { Text(selectedTimeFilter) },
                            leadingIcon = { Icon(Icons.Default.AccessTime, null) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF2C2C2C),
                                selectedContainerColor = SafetyYellow,
                                labelColor = Color.White,
                                selectedLabelColor = Color.Black
                            )
                        )
                        DropdownMenu(
                            expanded = expandedTime,
                            onDismissRequest = { expandedTime = false }
                        ) {
                            listOf("Most Recent", "Oldest", "Today", "This Week").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedTimeFilter = option
                                        expandedTime = false
                                    }
                                )
                            }
                        }
                    }

                    // Status filter
                    Box {
                        FilterChip(
                            selected = expandedStatus,
                            onClick = { expandedStatus = !expandedStatus },
                            label = { Text(selectedStatusFilter) },
                            leadingIcon = { Icon(Icons.Default.Assignment, null) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF2C2C2C),
                                selectedContainerColor = SafetyYellow,
                                labelColor = Color.White,
                                selectedLabelColor = Color.Black
                            )
                        )
                        DropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false }
                        ) {
                            listOf("All Status", "Pending Review", "Passed", "Failed", "Needs Attention").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedStatusFilter = option
                                        expandedStatus = false
                                    }
                                )
                            }
                        }
                    }

                    // Response filter
                    Box {
                        FilterChip(
                            selected = expandedResponse,
                            onClick = { expandedResponse = !expandedResponse },
                            label = { Text(selectedResponseFilter) },
                            leadingIcon = { Icon(Icons.Default.Check, null) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF2C2C2C),
                                selectedContainerColor = SafetyYellow,
                                labelColor = Color.White,
                                selectedLabelColor = Color.Black
                            )
                        )
                        DropdownMenu(
                            expanded = expandedResponse,
                            onDismissRequest = { expandedResponse = false }
                        ) {
                            listOf("All Responses", "Approved", "Rejected").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedResponseFilter = option
                                        expandedResponse = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
                .padding(bottom = 100.dp)
                .padding(top = 8.dp)
            ,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reports) { report ->
                SupervisorHistoryReportCard(report)
            }
        }
    }
}

// ---- Report Card for History ----
@Composable
fun SupervisorHistoryReportCard(report: FakeReport) {
    val dateFormat = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    val timeString = dateFormat.format(Date(report.time))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        // Use a Box to allow children to be positioned individually
        Box(modifier = Modifier.fillMaxWidth()) {
            // Main content in a Column
            Column(modifier = Modifier.padding(14.dp)) {
                Text(report.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Submitted: $timeString", fontSize = 17.sp, color = Color.Gray)
                Text("Inspector: ${report.inspectorName}", fontSize = 17.sp, color = Color.Gray)
                Text("Status: ${report.assignStatus}", fontSize = 17.sp, color = Color.Gray)

                // You can add more content here if needed
                if (report.hasNote) {
                    Spacer(Modifier.height(6.dp))
                    Text("📌 Note attached", fontSize = 13.sp, color = SafetyYellow)
                }
            }

            // 🎯 Response Tag: This is now a direct child of the Box and can be aligned
            Text(
                text = report.supervisorResponse.toString(), // Convert enum to string
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.TopEnd) // This is now a direct child of Box
                    .padding(8.dp)
                    .background(SafetyYellow, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}