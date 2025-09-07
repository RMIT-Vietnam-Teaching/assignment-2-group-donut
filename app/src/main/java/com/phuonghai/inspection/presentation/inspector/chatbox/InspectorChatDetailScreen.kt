package com.phuonghai.inspection.presentation.inspector.chatbox



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.phuonghai.inspection.domain.model.ChatMessage
import com.phuonghai.inspection.presentation.theme.DarkCharcoal
import com.phuonghai.inspection.presentation.theme.SafetyYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectorChatDetailScreen(
    navController: NavController,
    viewModel: InspectorChatDetailViewModel = hiltViewModel(),
) {
    val inspectorId = FirebaseAuth.getInstance().currentUser?.uid
    var inputMessage by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val supervisorName by viewModel.supervisorName.collectAsState()

    // Load messages once when screen appears
    LaunchedEffect(Unit) {
        if (inspectorId != null) {
            viewModel.getMessages(inspectorId)
        }
    }
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
                            "Chat with boss: $supervisorName",
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
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SafetyYellow)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
            ) {
                // 🔹 Chat history
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 20.dp).padding(bottom = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = true // show newest messages at bottom
                ) {
                    items(messages.size) { index ->
                        val message = messages[messages.size - 1 - index] // newest at bottom
                        ChatBubble1(
                            message = message,
                            isInspector = message.senderId == inspectorId
                        )
                    }
                }

                // 🔹 Input box (reuse from your old code)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 100.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputMessage,
                            onValueChange = { inputMessage = it },
                            placeholder = {
                                Text(
                                    "Write reply...",
                                    color = Color.Gray
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            textStyle = TextStyle(
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black,
                                focusedIndicatorColor = Color.Black,
                                unfocusedIndicatorColor = Color.Black
                            ),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (inputMessage.isNotBlank()) {
                                            inspectorId?.let {
                                                viewModel.sendMessage(it, inputMessage)
                                                inputMessage = "" // clear only after sending
                                            }
                                        }
                                    },
                                    enabled = inputMessage.isNotBlank()
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Send,
                                        contentDescription = "Send"
                                    )
                                }
                            }

                        )
                    }
                }
            }
        }
    }
}
@Composable
fun ChatBubble1(message: ChatMessage, isInspector: Boolean) {
    // 🔹 Convert millis → formatted string
    val formattedTime = remember(message.timestamp) {
        if (message.timestamp > 0) {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sdf.format(Date(message.timestamp))
        } else {
            ""
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isInspector) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = if (isInspector) SafetyYellow else Color(0xFF2C2C2C),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(10.dp)
                .widthIn(max = 270.dp) // limit bubble width
        ) {
            Text(
                text = message.text,
                color = if (isInspector) Color.Black else Color.White,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = formattedTime,
                fontSize = 12.sp,
                color = if (isInspector) Color.DarkGray else Color.LightGray
            )
        }
    }
}