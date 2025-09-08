package com.phuonghai.inspection.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SyncStatusComponent(
    isOnline: Boolean,
    isSyncing: Boolean,
    lastSyncTime: String? = null,
    pendingItemsCount: Int = 0,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Sync Status"
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSyncing -> Color(0xFF2196F3).copy(alpha = 0.1f)
                isOnline -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                else -> Color(0xFFFF9800).copy(alpha = 0.1f)
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = when {
                isSyncing -> Color(0xFF2196F3)
                isOnline -> Color(0xFF4CAF50)
                else -> Color(0xFFFF9800)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                NetworkStatusBadge(
                    isOnline = isOnline,
                    isSyncing = isSyncing
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content based on state
            when {
                isSyncing -> {
                    SyncingContent()
                }

                isOnline -> {
                    OnlineContent(
                        lastSyncTime = lastSyncTime,
                        pendingItemsCount = pendingItemsCount,
                        onSyncClick = onSyncClick
                    )
                }

                else -> {
                    OfflineContent(
                        pendingItemsCount = pendingItemsCount
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkStatusBadge(
    isOnline: Boolean,
    isSyncing: Boolean
) {
    Surface(
        color = when {
            isSyncing -> Color(0xFF2196F3)
            isOnline -> Color(0xFF4CAF50)
            else -> Color(0xFFFF9800)
        },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    color = Color.White,
                    strokeWidth = 1.5.dp
                )
            } else {
                Icon(
                    imageVector = if (isOnline) Icons.Default.Cloud else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = when {
                    isSyncing -> "Syncing"
                    isOnline -> "Online"
                    else -> "Offline"
                },
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SyncingContent() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = Color(0xFF2196F3),
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Synchronizing data...",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Please wait while we update your data",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun OnlineContent(
    lastSyncTime: String?,
    pendingItemsCount: Int,
    onSyncClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Connected",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4CAF50)
                )

                if (lastSyncTime != null) {
                    Text(
                        text = "Last sync: $lastSyncTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                if (pendingItemsCount > 0) {
                    Text(
                        text = "$pendingItemsCount items pending sync",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF9800)
                    )
                }
            }

            Button(
                onClick = onSyncClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sync",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun OfflineContent(
    pendingItemsCount: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.CloudOff,
            contentDescription = null,
            tint = Color(0xFFFF9800),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Working offline",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFFF9800)
            )
            Text(
                text = if (pendingItemsCount > 0) {
                    "$pendingItemsCount items will sync when online"
                } else {
                    "Data will sync automatically when connected"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SyncProgressIndicator(
    progress: Float,
    currentItem: String = "",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Syncing Progress",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF2196F3),
                trackColor = Color.Gray.copy(alpha = 0.3f)
            )

            if (currentItem.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Syncing: $currentItem",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun SyncResultDialog(
    isVisible: Boolean,
    isSuccess: Boolean,
    message: String,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (isSuccess) "Sync Successful" else "Sync Failed",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            },
            dismissButton = {
                if (!isSuccess && onRetry != null) {
                    TextButton(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
        )
    }
}