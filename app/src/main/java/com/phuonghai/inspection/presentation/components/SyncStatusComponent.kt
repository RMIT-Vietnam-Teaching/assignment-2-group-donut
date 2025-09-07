package com.phuonghai.inspection.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phuonghai.inspection.core.sync.SyncProgress
import com.phuonghai.inspection.presentation.theme.*

@Composable
fun SyncStatusComponent(
    viewModel: SyncStatusViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val syncProgress by viewModel.syncProgress.collectAsStateWithLifecycle()
    val unsyncedCount by viewModel.unsyncedCount.collectAsStateWithLifecycle()
    val isNetworkConnected by viewModel.isNetworkConnected.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadSyncStatus()
    }

    if (unsyncedCount > 0 || syncProgress !is SyncProgress.Idle) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { viewModel.startManualSync() },
            colors = CardDefaults.cardColors(
                containerColor = when {
                    !isNetworkConnected -> StatusOrange.copy(alpha = 0.1f)
                    syncProgress is SyncProgress.Error -> SafetyRed.copy(alpha = 0.1f)
                    else -> StatusBlue.copy(alpha = 0.1f)
                }
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = when {
                            !isNetworkConnected -> Icons.Default.CloudOff
                            syncProgress is SyncProgress.InProgress -> Icons.Default.Sync
                            syncProgress is SyncProgress.Error -> Icons.Default.Error
                            unsyncedCount > 0 -> Icons.Default.CloudUpload
                            else -> Icons.Default.CloudDone
                        },
                        contentDescription = null,
                        tint = when {
                            !isNetworkConnected -> StatusOrange
                            syncProgress is SyncProgress.Error -> SafetyRed
                            else -> StatusBlue
                        },
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = when {
                                !isNetworkConnected -> "Offline Mode"
                                syncProgress is SyncProgress.InProgress -> "Syncing Reports..."
                                syncProgress is SyncProgress.Error -> "Sync Failed"
                                unsyncedCount > 0 -> "$unsyncedCount Reports Pending"
                                else -> "All Reports Synced"
                            },
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // ✅ Fix smart cast issue by using local variable
                        val currentProgress = syncProgress
                        when (currentProgress) {
                            is SyncProgress.InProgress -> {
                                Text(
                                    text = "${currentProgress.current}/${currentProgress.total}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            is SyncProgress.Completed -> {
                                Text(
                                    text = "✓ ${currentProgress.success} synced, ${currentProgress.failed} failed",
                                    fontSize = 12.sp,
                                    color = if (currentProgress.failed > 0) SafetyRed else StatusGreen
                                )
                            }
                            is SyncProgress.Error -> {
                                Text(
                                    text = currentProgress.message,
                                    fontSize = 12.sp,
                                    color = SafetyRed
                                )
                            }
                            else -> {
                                if (!isNetworkConnected && unsyncedCount > 0) {
                                    Text(
                                        text = "Will sync when online",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Progress indicator or sync button
                val currentProgress = syncProgress
                when (currentProgress) {
                    is SyncProgress.InProgress -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = StatusBlue,
                            strokeWidth = 2.dp
                        )
                    }
                    else -> {
                        if (isNetworkConnected && unsyncedCount > 0) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync Now",
                                tint = StatusBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Progress bar for sync
            val currentProgress = syncProgress
            if (currentProgress is SyncProgress.InProgress) {
                LinearProgressIndicator(
                    progress = { currentProgress.current.toFloat() / currentProgress.total.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = StatusBlue,
                    trackColor = StatusBlue.copy(alpha = 0.3f)
                )
            }
        }
    }
}