package com.phuonghai.inspection.presentation.home.inspector

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phuonghai.inspection.presentation.home.inspector.notification.NotificationViewModel

data class NotificationItem(
    val id: String,
    val date: String,
    val title: String,
    val location: String,
    val preview: String,
    val isUnread: Boolean = false,
    val type: NotificationType = NotificationType.GENERAL
)

enum class NotificationType {
    GENERAL, URGENT, SUCCESS, WARNING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectorNotificationScreen(
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val safetyYellow = Color(0xFFFFD700)
    val darkCharcoal = Color(0xFF2C2C2C)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    // Sample notifications for testing
    val sampleNotifications = listOf(
        NotificationItem(
            id = "1",
            date = "1 Sept 2025",
            title = "Weekly Site Inspection Report",
            location = "Factory A | Line 3",
            preview = "Inspection completed. No major issues found, only ...",
            type = NotificationType.SUCCESS
        ),
        NotificationItem(
            id = "2",
            date = "31 Aug 2025",
            title = "Follow-up Required: Safety Violation",
            location = "Warehouse B",
            preview = "Critical issue detected in fire exit compliance. ...",
            isUnread = true,
            type = NotificationType.URGENT
        ),
        NotificationItem(
            id = "3",
            date = "28 Aug 2025",
            title = "Equipment Maintenance Checklist",
            location = "Construction Site C",
            preview = "Reminder: Complete machinery checks before next ...",
            type = NotificationType.WARNING
        ),
        NotificationItem(
            id = "4",
            date = "27 Aug 2025",
            title = "Inspection Feedback Submitted",
            location = "Retail Store D",
            preview = "Inspector feedback uploaded. Please review and a...",
            type = NotificationType.GENERAL
        ),
        NotificationItem(
            id = "5",
            date = "1 Sept 2025",
            title = "Weekly Site Inspection Report",
            location = "Factory A | Line 3",
            preview = "Inspection completed. No major issues found, only ...",
            type = NotificationType.SUCCESS
        ),
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkCharcoal
                ),
                actions = {
                    IconButton(onClick = { viewModel.markAllAsRead() }) {
                        Icon(
                            Icons.Default.DoneAll,
                            contentDescription = "Mark all read",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = safetyYellow)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                sampleNotifications.groupBy { it.date }.forEach { (date, items) ->
                    item {
                        Text(
                            text = date,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = safetyYellow,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(items) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = { viewModel.markAsRead(notification.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    val safetyYellow = Color(0xFFFFD700)

    val iconColor = when (notification.type) {
        NotificationType.SUCCESS -> Color(0xFF4CAF50)
        NotificationType.URGENT -> Color(0xFFE53935)
        NotificationType.WARNING -> Color(0xFFFFC107)
        NotificationType.GENERAL -> safetyYellow
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isUnread)
                Color(0xFF2A2A2A) else Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(6.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = when (notification.type) {
                    NotificationType.SUCCESS -> Icons.Default.CheckCircle
                    NotificationType.URGENT -> Icons.Default.Warning
                    NotificationType.WARNING -> Icons.Default.Info
                    NotificationType.GENERAL -> Icons.Default.Campaign
                },
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .size(28.dp)
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    notification.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    notification.location,
                    fontSize = 14.sp,
                    color = safetyYellow,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    notification.preview,
                    fontSize = 14.sp,
                    color = Color.LightGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (notification.isUnread) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(safetyYellow)
                )
            }
        }
    }
}