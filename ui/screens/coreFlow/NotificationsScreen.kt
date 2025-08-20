package com.example.ui_for_assignment2.ui.screens.coreFlow


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val DarkCharcoal = Color(0xFF1E1E1E)
private val OffWhite = Color(0xFFFAFAFA)
private val SafetyYellow = Color(0xFFFFD700)
private val StatusGreen = Color(0xFF4CAF50)
private val StatusRed = Color(0xFFE53935)
private val StatusOrange = Color(0xFFFB8C00)

@Composable
fun NotificationsScreen() {
    val notifications = listOf(
        NotificationItem(
            type = NotificationType.APPROVED,
            message = "Report A-102 approved ✅",
            time = "Today • 10:45"
        ),
        NotificationItem(
            type = NotificationType.REJECTED,
            message = "Report A-099 rejected ❌ • requires revisions",
            time = "Today • 09:12"
        ),
        NotificationItem(
            type = NotificationType.PENDING,
            message = "Report A-110 pending ⏳ • awaiting QA review",
            time = "Yesterday • 17:20"
        ),
        NotificationItem(
            type = NotificationType.APPROVED,
            message = "Report B-224 approved ✅",
            time = "Aug 18 • 14:03"
        ),
        NotificationItem(
            type = NotificationType.PENDING,
            message = "Report C-031 pending ⏳ • manager assigned",
            time = "Aug 18 • 08:47"
        ),
        NotificationItem(
            type = NotificationType.REJECTED,
            message = "Report D-210 rejected ❌ • incomplete photos",
            time = "Aug 17 • 19:30"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(notifications) { item ->
                NotificationListItem(item)
                Divider()
            }
        }
    }
}

private data class NotificationItem(
    val type: NotificationType,
    val message: String,
    val time: String
)

private enum class NotificationType {
    APPROVED, REJECTED, PENDING
}

@Composable
private fun NotificationListItem(item: NotificationItem) {
    val (icon, tint) = when (item.type) {
        NotificationType.APPROVED -> Icons.Filled.CheckCircle to StatusGreen
        NotificationType.REJECTED -> Icons.Filled.Cancel to StatusRed
        NotificationType.PENDING -> Icons.Filled.Schedule to StatusOrange
    }

    ListItem(
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = item.type.name,
                tint = tint
            )
        },
        headlineContent = {
            Text(
                text = item.message,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = item.time,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    )
}

@Preview(name = "Notifications - Light", showBackground = true)
@Composable
private fun PreviewNotificationsLight() {
    MaterialTheme { NotificationsScreen() }
}

@Preview(
    name = "Notifications - Dark",
    showBackground = true,
    backgroundColor = 0xFF1E1E1E
)
@Composable
private fun PreviewNotificationsDark() {
    MaterialTheme { NotificationsScreen() }
}
