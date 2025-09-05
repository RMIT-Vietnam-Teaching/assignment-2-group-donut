package com.donut.assignment2.presentation.supervisor.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.phuonghai.inspection.domain.model.Report
import com.phuonghai.inspection.presentation.supervisor.history.SupervisorHistoryViewModel
import com.phuonghai.inspection.presentation.theme.SafetyYellow
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorHistoryScreen(
    navController: NavController,
) {

    val viewModel: SupervisorHistoryViewModel = hiltViewModel()

    val reportsState by viewModel.reports.collectAsState()
    val isLoadingState by viewModel.isLoading.collectAsState()



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
        if(isLoadingState){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ){
                CircularProgressIndicator(color = Color.White)
            }
        }else{

            // ✅ Apply filters and sorting
            val filteredReports = reportsState
                // 🔍 Search filter
                .filter { report ->
                    searchQuery.isBlank() ||
                            report.title.contains(searchQuery, ignoreCase = true) ||
                            report.address?.contains(searchQuery, ignoreCase = true) == true
                }
                // 📌 Status filter
                .filter { report ->
                    when (selectedStatusFilter) {
                        "Pending Review" -> report.assignStatus.name == "PENDING"
                        "Passed" -> report.assignStatus.name == "PASSED"
                        "Failed" -> report.assignStatus.name == "FAILED"
                        "Needs Attention" -> report.assignStatus.name == "NEEDS_ATTENTION"
                        else -> true
                    }
                }
                // 📌 Response filter
                .filter { report ->
                    when (selectedResponseFilter) {
                        "Approved" -> report.responseStatus.name == "APPROVED"
                        "Rejected" -> report.responseStatus.name == "REJECTED"
                        else -> true
                    }
                }
                // 📌 Time sort
                .sortedWith(compareBy { report ->
                    when (selectedTimeFilter) {
                        "Most Recent" -> -(report.completedAt?.toDate()?.time ?: 0L)
                        "Oldest" -> report.completedAt?.toDate()?.time ?: 0L
                        "Today" -> if (isToday(report.completedAt?.toDate())) 0 else 1
                        "This Week" -> if (isThisWeek(report.completedAt?.toDate())) 0 else 1
                        else -> -(report.completedAt?.toDate()?.time ?: 0L)
                    }
                })

            // 👉 Replace LazyColumn with filtered list
            if (filteredReports.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No reports found",
                        color = SafetyYellow,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 100.dp)
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredReports.size) { index ->
                        SupervisorHistoryReportCard(filteredReports[index])
                    }
                }
            }
        }
    }
}

// ---- Report Card for History ----
@Composable
fun SupervisorHistoryReportCard(report: Report) {
    val dateFormat = SimpleDateFormat("EEE MMM dd yyyy", Locale.getDefault())
    val formattedDate = report.completedAt?.toDate()?.let { dateFormat.format(it) } ?: ""

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
                Text("Submitted: $formattedDate", fontSize = 17.sp, color = Color.Gray)
                Text("Inspector: ${"report.inspectorName"}", fontSize = 17.sp, color = Color.Gray)
                Text("Status: ${report.assignStatus}", fontSize = 17.sp, color = Color.Gray)
                Text("Location: ${report.address}", fontSize = 17.sp, color = Color.Gray)
            }

            // 🎯 Response Tag: This is now a direct child of the Box and can be aligned
            Text(
                text = report.responseStatus.toString(), // Convert enum to string
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
fun isToday(date: Date?): Boolean {
    if (date == null) return false
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal2.time = date
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun isThisWeek(date: Date?): Boolean {
    if (date == null) return false
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal2.time = date
    return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR) &&
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
}