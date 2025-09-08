package com.phuonghai.inspection.presentation.inspector.task

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val syncState by viewModel.syncState.collectAsStateWithLifecycle() // NEW
    val networkState by viewModel.networkState.collectAsStateWithLifecycle() // NEW

    // Load initial tasks and offline info
    LaunchedEffect(Unit) {
        viewModel.loadTasks()
        viewModel.getOfflineTasksInfo() // NEW
    }

    LaunchedEffect(navController) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getStateFlow("should_refresh_tasks", false)?.collect { shouldRefresh ->
            if (shouldRefresh) {
                Log.d("TaskScreen", "Refreshing tasks due to report submission")
                viewModel.refreshTasks()
                savedStateHandle["should_refresh_tasks"] = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "My Tasks",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OffWhite
                        )

                        // NEW: Online/Offline indicator
                        Spacer(modifier = Modifier.width(8.dp))
                        NetworkStatusIndicator(
                            isOnline = networkState,
                            modifier = Modifier
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkCharcoal
                ),
                actions = {
                    // NEW: Sync button
                    IconButton(
                        onClick = { viewModel.syncTasks() },
                        enabled = networkState
                    ) {
                        when (syncState) {
                            is TaskSyncUiState.Syncing -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = OffWhite,
                                    strokeWidth = 2.dp
                                )
                            }
                            else -> {
                                Icon(
                                    Icons.Default.Sync,
                                    contentDescription = "Sync Tasks",
                                    tint = if (networkState) OffWhite else OffWhite.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    IconButton(onClick = { viewModel.refreshTasks() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = OffWhite
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // NEW: Sync Status Card
            SyncStatusCard(
                syncState = syncState,
                offlineTasksCount = uiState.offlineTasksCount,
                isOnline = networkState,
                onSyncClick = { viewModel.syncTasks() },
                onDismissSync = { viewModel.clearSyncStatus() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                placeholder = { Text("Search tasks...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = SafetyYellow,
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Priority Filter
                DropdownFilter(
                    label = "Priority",
                    options = listOf("All", "High", "Medium", "Low"),
                    selectedOption = uiState.selectedPriority,
                    onOptionSelected = viewModel::updatePriorityFilter,
                    modifier = Modifier.weight(1f)
                )

                // Status Filter
                DropdownFilter(
                    label = "Status",
                    options = listOf("All", "ASSIGNED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "OVERDUE"),
                    selectedOption = uiState.selectedStatus,
                    onOptionSelected = viewModel::updateStatusFilter,
                    modifier = Modifier.weight(1f)
                )

                // Due Date Filter
                DropdownFilter(
                    label = "Due Date",
                    options = listOf("All", "Today", "This Week", "This Month"),
                    selectedOption = uiState.selectedDateFilter,
                    onOptionSelected = viewModel::updateDateFilter,
                    modifier = Modifier.weight(1f)
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
                        message = uiState.errorMessage ?: "Unknown error occurred",
                        onRetry = { viewModel.refreshTasks() },
                        onDismiss = { viewModel.clearError() }
                    )
                }

                filteredTasks.isEmpty() -> {
                    EmptyTasksContent(
                        isOnline = networkState,
                        onRetry = { viewModel.refreshTasks() }
                    )
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredTasks) { taskWithDetails ->
                            TaskCard(
                                taskWithDetails = taskWithDetails,
                                onClick = {
                                    navController.navigate(
                                        Screen.InspectorNewReport.createRoute(taskWithDetails.task.taskId)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// NEW: Sync Status Card Component
@Composable
fun SyncStatusCard(
    syncState: TaskSyncUiState,
    offlineTasksCount: Int,
    isOnline: Boolean,
    onSyncClick: () -> Unit,
    onDismissSync: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (syncState) {
                is TaskSyncUiState.Success -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                is TaskSyncUiState.Error -> Color(0xFFF44336).copy(alpha = 0.1f)
                is TaskSyncUiState.Syncing -> Color(0xFF2196F3).copy(alpha = 0.1f)
                else -> if (isOnline) Color.Transparent else Color(0xFFFF9800).copy(alpha = 0.1f)
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = when (syncState) {
                is TaskSyncUiState.Success -> Color(0xFF4CAF50)
                is TaskSyncUiState.Error -> Color(0xFFF44336)
                is TaskSyncUiState.Syncing -> Color(0xFF2196F3)
                else -> if (isOnline) Color.Gray.copy(alpha = 0.3f) else Color(0xFFFF9800)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            when (syncState) {
                is TaskSyncUiState.Idle -> {
                    if (!isOnline && offlineTasksCount > 0) {
                        OfflineStatusContent(
                            taskCount = offlineTasksCount,
                            onSyncClick = onSyncClick
                        )
                    } else if (isOnline && offlineTasksCount > 0) {
                        OnlineStatusContent(
                            taskCount = offlineTasksCount,
                            onSyncClick = onSyncClick
                        )
                    }
                }

                is TaskSyncUiState.Syncing -> {
                    SyncingStatusContent()
                }

                is TaskSyncUiState.Success -> {
                    SuccessStatusContent(
                        taskCount = syncState.taskCount,
                        onDismiss = onDismissSync
                    )
                }

                is TaskSyncUiState.Error -> {
                    ErrorStatusContent(
                        message = syncState.message,
                        onRetry = onSyncClick,
                        onDismiss = onDismissSync
                    )
                }
            }
        }
    }
}

@Composable
private fun OfflineStatusContent(
    taskCount: Int,
    onSyncClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CloudOff,
                contentDescription = null,
                tint = Color(0xFFFF9800)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Offline Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$taskCount tasks available offline",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        TextButton(
            onClick = onSyncClick,
            enabled = false
        ) {
            Text("Sync when online")
        }
    }
}

@Composable
private fun OnlineStatusContent(
    taskCount: Int,
    onSyncClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Cloud,
                contentDescription = null,
                tint = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Online",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$taskCount tasks cached locally",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        Button(
            onClick = onSyncClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = SafetyYellow,
                contentColor = DarkCharcoal
            )
        ) {
            Text("Sync Now")
        }
    }
}

@Composable
private fun SyncingStatusContent() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = Color(0xFF2196F3),
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Syncing tasks...",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun SuccessStatusContent(
    taskCount: Int,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sync successful: $taskCount tasks updated",
                style = MaterialTheme.typography.titleMedium
            )
        }
        IconButton(onClick = onDismiss) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.Gray
            )
        }
    }
}

@Composable
private fun ErrorStatusContent(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = Color(0xFFF44336)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sync failed",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFF44336)
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Gray
                )
            }
        }

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onRetry) {
                Text(
                    "Try Again",
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

// NEW: Network Status Indicator
@Composable
fun NetworkStatusIndicator(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isOnline) Icons.Default.Cloud else Icons.Default.CloudOff,
            contentDescription = if (isOnline) "Online" else "Offline",
            tint = if (isOnline) Color(0xFF4CAF50) else Color(0xFFFF9800),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (isOnline) "Online" else "Offline",
            style = MaterialTheme.typography.labelSmall,
            color = if (isOnline) Color(0xFF4CAF50) else Color(0xFFFF9800)
        )
    }
}

// Enhanced Empty Content with offline context
@Composable
fun EmptyTasksContent(
    isOnline: Boolean,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isOnline) Icons.Default.Assignment else Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isOnline) "No tasks found" else "No tasks available offline",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isOnline)
                "Try adjusting your filters or refresh to load new tasks"
            else
                "Connect to internet to download your tasks",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = SafetyYellow,
                contentColor = DarkCharcoal
            ),
            enabled = isOnline
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isOnline) "Refresh" else "Refresh when online")
        }
    }
}

// Enhanced Error Content
@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, Color(0xFFF44336))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = Color(0xFFF44336)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF44336)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
                }
            }

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onRetry) {
                    Text(
                        "Try Again",
                        color = Color(0xFF2196F3)
                    )
                }
            }
        }
    }
}

// Enhanced Task Card with offline indicators
@Composable
fun TaskCard(
    taskWithDetails: TaskWithDetails,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with title and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = taskWithDetails.task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                TaskStatusChip(status = taskWithDetails.task.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = taskWithDetails.task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Branch info
                Column {
                    Text(
                        text = "Branch",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = taskWithDetails.branch?.name ?: "Unknown",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Due date
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Due Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = taskWithDetails.task.dueDate?.let {
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                .format(Date(it.seconds * 1000))
                        } ?: "No due date",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Priority and report status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriorityChip(priority = taskWithDetails.task.priority)

                if (taskWithDetails.hasReport) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Has Report",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskStatusChip(status: TaskStatus) {
    val (backgroundColor, textColor) = when (status) {
        TaskStatus.ASSIGNED -> Color(0xFF2196F3) to Color.White
        TaskStatus.IN_PROGRESS -> Color(0xFFFF9800) to Color.White
        TaskStatus.COMPLETED -> Color(0xFF4CAF50) to Color.White
        TaskStatus.CANCELLED -> Color(0xFFF44336) to Color.White
        TaskStatus.OVERDUE -> Color(0xFF9C27B0) to Color.White
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PriorityChip(priority: String) {
    val (backgroundColor, textColor) = when (priority.lowercase()) {
        "high" -> Color(0xFFF44336) to Color.White
        "medium" -> Color(0xFFFF9800) to Color.White
        "low" -> Color(0xFF4CAF50) to Color.White
        else -> Color.Gray to Color.White
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = priority,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownFilter(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = SafetyYellow,
                unfocusedBorderColor = Color.Gray
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}