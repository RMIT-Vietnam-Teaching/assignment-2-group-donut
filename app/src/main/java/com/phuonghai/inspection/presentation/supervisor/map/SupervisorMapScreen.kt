package com.donut.assignment2.presentation.supervisor.map

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition.Center.position
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.phuonghai.inspection.domain.model.Branch
import com.phuonghai.inspection.domain.model.Task
import com.phuonghai.inspection.presentation.supervisor.map.SupervisorMapViewModel
import com.phuonghai.inspection.presentation.theme.SafetyYellow

@Composable
fun SupervisorMapScreen(navController: NavController) {
    val viewModel: SupervisorMapViewModel = hiltViewModel()
    val branches by viewModel.branches.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val error by viewModel.error.collectAsState()

    var expandedStatus by remember { mutableStateOf(false) }
    var selectedStatusFilter by remember { mutableStateOf("All Status") }
    var expandedPriority by remember { mutableStateOf(false) }
    var selectedPriorityFilter by remember { mutableStateOf("All Priority") }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(1.35, 103.87), 5f)
    }

    var selectedBranch by remember { mutableStateOf<Branch?>(null) }


    // Move camera when branches are loaded
    LaunchedEffect(branches) {
        if (branches.isNotEmpty()) {
            val firstBranch = branches[0]
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(firstBranch.lat.toDouble(), firstBranch.lng.toDouble()),
                12f
            )
        }
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp),
        cameraPositionState = cameraPositionState
    ) {
        branches.forEach { branch ->
            Marker(
                state = MarkerState(
                    position = LatLng(branch.lat.toDouble(), branch.lng.toDouble())
                ),
                title = branch.branchName,
                snippet = branch.address,
                onClick = {
                    selectedBranch = branch
                    viewModel.loadTasksByBranch(branch.branchId)
                    true
                }
            )
        }
    }

    // Dialog with branch + summary info
    if (selectedBranch != null) {
        AlertDialog(
            onDismissRequest = { selectedBranch = null },
            title = {
                Text(text = selectedBranch?.branchName ?: "Branch Info")
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("📍 Address: ${selectedBranch?.address ?: "N/A"}", style = MaterialTheme.typography.bodyMedium, fontSize = 18.sp)

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()



                    Spacer(modifier = Modifier.height(8.dp))

                    // Tasks summary
                    // 🛠 Tasks header + "See all"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🛠 Tasks (${tasks.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        // Time filter
                        Box {
                            FilterChip(
                                selected = expandedPriority,
                                onClick = { expandedPriority = !expandedPriority },
                                label = { Text(selectedPriorityFilter) },
                                leadingIcon = { Icon(Icons.Default.AccessTime, null) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFF2C2C2C),
                                    selectedContainerColor = SafetyYellow,
                                    labelColor = Color.White,
                                    selectedLabelColor = Color.Black
                                )
                            )
                            DropdownMenu(
                                expanded = expandedPriority,
                                onDismissRequest = { expandedPriority = false }
                            ) {
                                listOf("All priority","HIGH", "NORMAL", "LOW").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            selectedPriorityFilter = option
                                            expandedPriority = false
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
                                listOf("All Status", "ASSIGNED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "OVERDUE").forEach { option ->
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
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    when {
                        error != null -> Text("⚠️ Error: $error", color = Color.Red)
                        tasks.isEmpty() -> Text("No tasks available", color = Color.Gray)
                        else -> {
                            // Apply filters
                            val filteredTasks = tasks.filter { task ->
                                val matchesPriority = when (selectedPriorityFilter) {
                                    "HIGH", "NORMAL", "LOW" -> task.priority.name == selectedPriorityFilter
                                    else -> true // "All" case
                                }
                                val matchesStatus = when (selectedStatusFilter) {
                                    "ASSIGNED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "OVERDUE" ->
                                        task.status.name == selectedStatusFilter
                                    else -> true // "All Status"
                                }
                                matchesPriority && matchesStatus
                            }
                            if(filteredTasks.isEmpty()){
                                Text("No tasks match the selected filters", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 16.sp)
                            }else{
                                // You can also sort filteredTasks here if needed
                                val sortedTasks = filteredTasks.sortedBy { it.priority } // example

                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp) // limit height so dialog doesn’t grow too big
                                ) {
                                    items(sortedTasks.take(10)) { task ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Priority indicator color
                                            val priorityColor = when (task.priority.name) {
                                                "HIGH" -> Color.Red
                                                "NORMAL" -> Color(0xFFFFA000) // Amber
                                                "LOW" -> Color(0xFF4CAF50) // Green
                                                else -> Color.Gray
                                            }

                                            // Status color
                                            val statusColor = when (task.status.name) {
                                                "ASSIGNED" -> Color(0xFFFFA000) // Amber
                                                "COMPLETED" -> Color(0xFF4CAF50) // Green
                                                "IN_PROGRESS" -> Color(0xFF2196F3) // Blue
                                                "OVERDUE" -> Color.Red
                                                "CANCELLED" -> Color.Gray
                                                else -> Color.DarkGray
                                            }

                                            Text(
                                                "${task.title}",
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = task.status.name,
                                                color = statusColor,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(end = 6.dp)
                                            )
                                            Text(
                                                text = "[${task.priority.name}]",
                                                color = priorityColor,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Divider()
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedBranch = null }) {
                    Text("Close")
                }
            }
        )
    }
}
