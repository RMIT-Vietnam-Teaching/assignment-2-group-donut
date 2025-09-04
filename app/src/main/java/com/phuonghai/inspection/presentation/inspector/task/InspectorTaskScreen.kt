package com.phuonghai.inspection.presentation.inspector.task

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.phuonghai.inspection.domain.common.Priority
import com.phuonghai.inspection.domain.model.TaskStatus
import com.phuonghai.inspection.presentation.navigation.Screen
import com.phuonghai.inspection.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectorTaskScreen(
    navController: NavController,
    viewModel: InspectorTaskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredTasks by viewModel.filteredTasks.collectAsStateWithLifecycle()

    // Load tasks when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadTasks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Tasks",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OffWhite
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkCharcoal
                ),
                actions = {
                    IconButton(onClick = { viewModel.refreshTasks() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = SafetyYellow
                        )
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                label = { Text("Search tasks") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = SafetyYellow)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SafetyYellow,
                    unfocusedBorderColor = BorderDark,
                    focusedLabelColor = SafetyYellow,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = OffWhite,
                    unfocusedTextColor = OffWhite,
                    cursorColor = SafetyYellow,
                    focusedContainerColor = InputBgDark,
                    unfocusedContainerColor = InputBgDark
                )
            )

            Spacer(modifier = Modifier.height(12.dp))


            // Filters Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Priority Dropdown
                DropdownFilter(
                    label = "Priority",
                    options = listOf("All", "HIGH", "NORMAL", "LOW"),
                    selectedOption = uiState.selectedPriority,
                    onOptionSelected = viewModel::updatePriorityFilter,
                    modifier = Modifier.weight(1f)
                )

                // Status Dropdown
                DropdownFilter(
                    label = "Status",
                    options = listOf("All", "ASSIGNED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "OVERDUE"),
                    selectedOption = uiState.selectedStatus,
                    onOptionSelected = viewModel::updateStatusFilter,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                DropdownFilter(
                    label = "Due Date",
                    options = listOf("All", "Today", "This Week", "This Month"),
                    selectedOption = uiState.selectedDateFilter,
                    onOptionSelected = viewModel::updateDateFilter,
                    modifier = Modifier.width(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content based on state
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SafetyYellow)
                    }
                }

                uiState.showError -> {
                    ErrorContent(
                        message = uiState.errorMessage ?: "Có lỗi xảy ra",
                        onRetry = { viewModel.refreshTasks() },
                        onDismiss = { viewModel.clearError() }
                    )
                }

                uiState.isEmpty -> {
                    EmptyTasksContent()
                }

                else -> {
                    // Task list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 110.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredTasks) { taskWithDetails ->
                            TaskCard(
                                taskWithDetails = taskWithDetails,
                                onStatusUpdate = { taskId, newStatus ->
                                    viewModel.updateTaskStatus(taskId, newStatus)
                                },
                                onCreateReportClick = {
                                    navController.navigate(Screen.InspectorNewReportScreen.route)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownFilter(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            border = BorderStroke(1.dp, SafetyYellow),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = SafetyYellow
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "$label: $selectedOption",
                fontSize = 14.sp,
                color = SafetyYellow,
                maxLines = 1
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = OffWhite) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TaskCard(
    taskWithDetails: TaskWithDetails,
    onStatusUpdate: (String, TaskStatus) -> Unit,
    onCreateReportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val task = taskWithDetails.task
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        ),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Priority Tag (top-right corner)
            Surface(
                color = when (task.priority) {
                    Priority.HIGH -> SafetyRed
                    Priority.NORMAL -> StatusBlue
                    Priority.LOW -> StatusGray
                },
                shape = RoundedCornerShape(bottomStart = 8.dp),
                tonalElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
            ) {
                Text(
                    text = task.priority.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Card Content
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(top = 24.dp), // Extra padding for priority tag
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = OffWhite
                )

                Text(
                    "Description: ${task.description}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    color = TextLight
                )

                Text(
                    "Branch: ${taskWithDetails.branchName}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 15.sp,
                    color = SafetyYellow
                )

                Text(
                    "Supervisor: ${taskWithDetails.supervisorName}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 15.sp,
                    color = TextSecondary
                )

                Text(
                    "Due: ${task.dueDate?.toDate()?.let { dateFormat.format(it) } ?: "No due date"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 14.sp,
                    color = if (isTaskOverdue(task)) SafetyRed else TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status indicator
                    Surface(
                        color = when (task.status) {
                            TaskStatus.ASSIGNED -> StatusBlue
                            TaskStatus.IN_PROGRESS -> StatusOrange
                            TaskStatus.COMPLETED -> StatusGreen
                            TaskStatus.CANCELLED -> StatusGray
                            TaskStatus.OVERDUE -> SafetyRed
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = when (task.status) {
                                TaskStatus.ASSIGNED -> "Assigned"
                                TaskStatus.IN_PROGRESS -> "In Progress"
                                TaskStatus.COMPLETED -> "Completed"
                                TaskStatus.CANCELLED -> "Cancelled"
                                TaskStatus.OVERDUE -> "Overdue"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Status update button
                        if (task.status != TaskStatus.COMPLETED && task.status != TaskStatus.CANCELLED) {
                            Button(
                                onClick = {
                                    val newStatus = when (task.status) {
                                        TaskStatus.ASSIGNED -> TaskStatus.IN_PROGRESS
                                        TaskStatus.IN_PROGRESS -> TaskStatus.COMPLETED
                                        else -> TaskStatus.IN_PROGRESS
                                    }
                                    onStatusUpdate(task.taskId, newStatus)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StatusOrange
                                ),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = when (task.status) {
                                        TaskStatus.ASSIGNED -> "Start"
                                        TaskStatus.IN_PROGRESS -> "Complete"
                                        else -> "Update"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Create report button
                        Button(
                            onClick = onCreateReportClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SafetyYellow
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "New Report",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTasksContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Assignment,
            contentDescription = "No Tasks",
            modifier = Modifier.size(64.dp),
            tint = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Tasks Available",
            style = MaterialTheme.typography.titleMedium,
            color = OffWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You don't have any tasks assigned yet. Check back later or contact your supervisor.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    "Error",
                    color = OffWhite
                )
            },
            text = {
                Text(
                    message,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = onRetry) {
                    Text(
                        "Retry",
                        color = SafetyYellow
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        "Close",
                        color = TextSecondary
                    )
                }
            },
            containerColor = SurfaceDark
        )
    }
}

// Helper function to check if task is overdue
private fun isTaskOverdue(task: com.phuonghai.inspection.domain.model.Task): Boolean {
    val dueDate = task.dueDate?.toDate()?.time ?: return false
    val currentTime = System.currentTimeMillis()
    return currentTime > dueDate && task.status != TaskStatus.COMPLETED
}