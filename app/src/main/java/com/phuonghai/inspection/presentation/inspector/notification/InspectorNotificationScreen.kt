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
import com.google.firebase.Timestamp
import com.phuonghai.inspection.domain.model.Notification
import com.phuonghai.inspection.domain.model.NotificationType
import com.phuonghai.inspection.presentation.home.inspector.notification.InspectorNotificationViewModel
import com.phuonghai.inspection.presentation.theme.SafetyYellow
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectorNotificationScreen(
    viewModel: InspectorNotificationViewModel = hiltViewModel()
) {
    val safetyYellow = Color(0xFFFFD700)
    val darkCharcoal = Color(0xFF2C2C2C)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    fun formatDate(timestamp: Timestamp?): String {
        if (timestamp == null) return ""
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkCharcoal
                )
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
                uiState.notifications
                    .sortedByDescending { it.date }
                    .groupBy { formatDate(it.date) }
                    .forEach { (date, items) ->
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
                            onClick = { }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit
) {

    val iconColor = when (notification.type) {
        NotificationType.TASK_ASSIGNED -> Color(0xFF4CAF50)
        NotificationType.REPORT_ACCEPTED -> Color(0xFFE53935)
        NotificationType.REPORT_REJECTED -> Color(0xFFFFC107)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
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
                    NotificationType.REPORT_REJECTED -> Icons.Default.Warning
                    NotificationType.REPORT_ACCEPTED -> Icons.Default.Done
                    NotificationType.TASK_ASSIGNED -> Icons.Default.Assignment
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
                    notification.message,
                    fontSize = 14.sp,
                    color = SafetyYellow,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    notification.type.name,
                    fontSize = 14.sp,
                    color = Color.LightGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}