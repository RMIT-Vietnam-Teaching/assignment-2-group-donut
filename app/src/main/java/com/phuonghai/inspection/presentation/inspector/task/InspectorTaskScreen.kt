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
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val networkState by viewModel.networkState.collectAsStateWithLifecycle()

    // Load initial tasks and offline info
    LaunchedEffect(Unit) {
        viewModel.loadTasks()
        viewModel.getOfflineTasksInfo()
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

                        Spacer(modifier = Modifier.width(8.dp))
                        TaskNetworkStatusIndicator(
                            isOnline = networkState,
                            modifier = Modifier
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkCharcoal
                ),
                actions = {
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

            // Sync Status Card
            TaskSyncStatusCard(
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
                label = { Text("Search tasks", color = TextSecondary) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = SafetyYellow)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DropdownFilter(
                    label = "Priority",
                    options = listOf("All", "HIGH", "NORMAL", "LOW"),
                    selectedOption = uiState.selectedPriority,
                    onOptionSelected = viewModel::updatePriorityFilter,
                    modifier = Modifier.weight(1f)
                )

                DropdownFilter(
                    label = "Status",
                    options = listOf("All", "ASSIGNED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "OVERDUE"),
                    selectedOption = uiState.selectedStatus,
                    onOptionSelected = viewModel::updateStatusFilter,
                    modifier = Modifier.weight(1f)
                )

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
                        message = uiState.errorMessage ?: "Có lỗi xảy ra",
                        onRetry = { viewModel.refreshTasks() },
                        onDismiss = { viewModel.clearError() }
                    )
                }

                filteredTasks.isEmpty() -> {
                    TaskEmptyContent(
                        isOnline = networkState,
                        onRetry = { viewModel.refreshTasks() }
                    )
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
                                    Log.d("TaskScreen", "Navigating to new report with taskId: ${taskWithDetails.task.taskId}")
                                    navController.navigate("${Screen.InspectorNewReportScreen.route}?taskId=${taskWithDetails.task.taskId}")
                                },
                                onContinueDraftClick = {
                                    taskWithDetails.draftReport?.let { draft ->
                                        Log.d("TaskScreen", "Navigating to continue draft with reportId: ${draft.reportId}")
                                        navController.navigate("${Screen.InspectorNewReportScreen.route}?reportId=${draft.reportId}")
                                    }
                                },
                                hasDraft = taskWithDetails.hasDraft
                            )
                        }
                    }
                }
            }
        }
    }
}

// Task-specific Sync Status Card
@Composable
fun TaskSyncStatusCard(
    syncState: TaskSyncUiState,
    offlineTasksCount: Int,
    isOnline: Boolean,
    onSyncClick: () -> Unit,
    onDismissSync: () -> Unit
) {
    if (syncState == TaskSyncUiState.Idle && isOnline && offlineTasksCount == 0) {
        return
    }

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
                        TaskOfflineContent(
                            taskCount = offlineTasksCount,
                            onSyncClick = onSyncClick
                        )
                    } else if (isOnline && offlineTasksCount > 0) {
                        TaskOnlineContent(
                            taskCount = offlineTasksCount,
                            onSyncClick = onSyncClick
                        )
                    }
                }

                is TaskSyncUiState.Syncing -> {
                    TaskSyncingContent()
                }

                is TaskSyncUiState.Success -> {
                    TaskSuccessContent(
                        taskCount = syncState.taskCount,
                        onDismiss = onDismissSync
                    )
                }

                is TaskSyncUiState.Error -> {
                    TaskErrorSyncContent(
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
private fun TaskOfflineContent(
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
private fun TaskOnlineContent(
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
private fun TaskSyncingContent() {
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
private fun TaskSuccessContent(
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
private fun TaskErrorSyncContent(
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

@Composable
fun TaskNetworkStatusIndicator(
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

@Composable
fun TaskEmptyContent(
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
    onContinueDraftClick: () -> Unit,
    hasDraft: Boolean,
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

            if (hasDraft && taskWithDetails.draftReport != null) {
                val draftReport = taskWithDetails.draftReport!!
                Surface(
                    color = StatusOrange,
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "DRAFT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = draftReport.createdAt?.toDate()?.let {
                                SimpleDateFormat("dd/MM", Locale.getDefault()).format(it)
                            } ?: "",
                            fontSize = 8.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Card Content
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(top = 24.dp),
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

                // Draft info display
                if (hasDraft && taskWithDetails.draftReport != null) {
                    val draftReport = taskWithDetails.draftReport!!
                    Text(
                        "Draft: \"${draftReport.title}\" - ${draftReport.createdAt?.toDate()?.let {
                            SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(it)
                        }}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 13.sp,
                        color = StatusOrange,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }

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
                        // Chỉ hiển thị các nút hành động khi task chưa hoàn thành hoặc chưa bị hủy
                        if (task.status != TaskStatus.COMPLETED && task.status != TaskStatus.CANCELLED) {

                            // Nút 1: Cập nhật trạng thái
                            when (task.status) {
                                TaskStatus.ASSIGNED -> {
                                    Button(
                                        onClick = { onStatusUpdate(task.taskId, TaskStatus.IN_PROGRESS) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = StatusBlue
                                        ),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Start", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                TaskStatus.IN_PROGRESS -> {
                                    // ======= ĐOẠN MÃ ĐÃ SỬA LỖI =======
                                    OutlinedButton(
                                        onClick = { /* Do nothing */ },
                                        enabled = false,
                                        modifier = Modifier.height(32.dp),
                                        border = BorderStroke(1.dp, StatusOrange.copy(alpha = 0.5f)), // Sửa ở đây
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            disabledContentColor = StatusOrange // Chỉ dùng tham số hợp lệ
                                        )
                                    ) {
                                        Text("In Progress", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                else -> {}
                            }

                            // Nút 2: Tạo/Tiếp tục Báo cáo
                            Button(
                                onClick = if (hasDraft) onContinueDraftClick else onCreateReportClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasDraft) StatusOrange else SafetyYellow
                                ),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = if (hasDraft) "Continue Draft" else "New Report",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasDraft) Color.White else Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
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