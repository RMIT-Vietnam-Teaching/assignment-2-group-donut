package com.phuonghai.inspection.presentation.supervisor.chatbox


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.phuonghai.inspection.presentation.theme.DarkCharcoal
import com.phuonghai.inspection.presentation.theme.SafetyYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorChatDetailScreen(
    inspectorId: String,
    navController: NavController,
) {
    // 🔹 Example chat history (later replace with ViewModel / Firestore data)
    val messages = listOf(
        ChatMessage("1", inspectorId, "Hello Supervisor, I’ve uploaded the report.", "09:00 AM"),
        ChatMessage("2", "supervisor123", "Got it. I’ll review it soon.", "09:05 AM"),
        ChatMessage("3", inspectorId, "Thanks! Please let me know if revisions are needed.", "09:10 AM"),
        ChatMessage("4", "supervisor123", "Sure, I’ll update you today.", "09:15 AM"),
    )
    val supervisorId = "supervisor123"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Text(
                            "Chat with $inspectorId",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCharcoal),
            )
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
        ) {
            // 🔹 Chat history
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = true // show newest messages at bottom
            ) {
                items(messages.size) { index ->
                    val message = messages[messages.size - 1 - index] // keep reverse layout consistent
                    ChatBubble(message, isSupervisor = message.senderId == supervisorId)
                }
            }

            // 🔹 Input box (reuse from your old code)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = { /* TODO: bind with state */ },
                    placeholder = { Text("Type a message...", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SafetyYellow,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = SafetyYellow
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { /* TODO: send message */ },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyYellow)
                ) {
                    Text("Send", color = Color.Black)
                }
            }
        }
    }
}
@Composable
fun ChatBubble(message: ChatMessage, isSupervisor: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSupervisor) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = if (isSupervisor) SafetyYellow else Color(0xFF2C2C2C),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(10.dp)
                .widthIn(max = 250.dp) // limit bubble width
        ) {
            Text(
                text = message.text,
                color = if (isSupervisor) Color.Black else Color.White,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = message.timestamp,
                fontSize = 12.sp,
                color = if (isSupervisor) Color.DarkGray else Color.LightGray
            )
        }
    }
}