package com.phuonghai.inspection.presentation.supervisor.report

import android.media.browse.MediaBrowser
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.phuonghai.inspection.domain.common.Priority
import com.phuonghai.inspection.domain.model.ResponseStatus
import com.phuonghai.inspection.presentation.generalUI.ButtonUI
import com.phuonghai.inspection.presentation.theme.DarkCharcoal
import com.phuonghai.inspection.presentation.theme.SafetyYellow
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorReportDetailScreen(
    reportId: String,
    navController: NavController,
    viewModel: SupervisorReportDetailViewModel = hiltViewModel(),
) {
    var showChatPopup by remember { mutableStateOf(false) }
    val reportState by viewModel.report.collectAsState()
    val isLoadingState by viewModel.isLoading.collectAsState()
    val report = reportState

    LaunchedEffect(reportId) {
        viewModel.loadReport(reportId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.clickable { navController.popBackStack() }
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Report Detail", color = Color.White)
                        }
                        ButtonUI(
                            text = "Chat With Inspector",
                            onClick = { showChatPopup = true },
                            backgroundColor = SafetyYellow,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCharcoal),
            )
        },
        containerColor = DarkCharcoal
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoadingState) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    CircularProgressIndicator(color = SafetyYellow)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    if (report == null) {
                        item { Text("Loading...", color = Color.White) }
                    } else {
                        // ✅ Media Section
                        item {
                            Column {
                                if (report.imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = report.imageUrl,
                                        contentDescription = "Report Image",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                } else {
                                    Text("No media attached", color = Color.Gray)
                                }
                            }
                        }

                        // ✅ Title & Priority
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = report.title,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = report.priority.name,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(color = when (report.priority.name) {
                                            "HIGH" -> Color(0xFFD32F2F)
                                            "NORMAL" -> Color.Gray
                                            "LOW" -> Color(0xFF388E3C)
                                            else -> Color.Gray
                                        },
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // ✅ Basic Info
                        item {
                            InfoRow("Inspector", report.inspectorId)
                            InfoRow("Type", report.type.name)
                            InfoRow("Address", report.address)
                            InfoRow("Latitude", report.lat)
                            InfoRow("Longitude", report.lng)
                        }

                        // ✅ Dates
                        item {
                            val dateFormat = SimpleDateFormat("EEE MMM dd yyyy", Locale.getDefault())
                            InfoRow("Created At", report.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "-")
                            InfoRow("Completed At", report.completedAt?.toDate()?.let { dateFormat.format(it) } ?: "-")
                        }

                        // ✅ Statuses
                        item {
                            InfoRow("Assign Status", report.assignStatus.name)
                            InfoRow("Response Status", report.responseStatus.name)
                            InfoRow("Sync Status", report.syncStatus.name)
                        }

                        // ✅ Details
                        item {
                            InfoRow("Description", report.description)
                            InfoRow("Score", report.score?.toString() ?: "N/A")
                            InfoRow("Review Notes", report.reviewNotes.ifBlank { "No review yet" })
                            InfoRow("Reviewed By", report.reviewedBy.ifBlank { "-" })
                        }
                        item{
                            if(report.responseStatus == ResponseStatus.PENDING){
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    Button(
                                        onClick = {
                                            viewModel.approveReport(reportId)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                    ){
                                        Text("Approved", color = Color.White)
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.rejectReport(reportId)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                                    ){
                                        Text("Rejected", color = Color.White)
                                    }
                                }
                            } else if(report.responseStatus == ResponseStatus.APPROVED){
                                Text("Report has been approved",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF4CAF50),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold)
                            } else if(report.responseStatus == ResponseStatus.REJECTED){
                                Text("Report has been rejected",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    color =  Color(0xFFE53935),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                if (showChatPopup) {
                    report?.inspectorId?.let {
                        ChatPopup(
                            inspectorId = it,
                            messages = sampleMessages,
                            onDismiss = { showChatPopup = false },
                            onSendMessage = { msg -> println("Send: $msg") }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
    }
}
@Composable
fun ChatPopup(
    onDismiss: () -> Unit,
    inspectorId: String,
    messages: List<ChatMessage>, // Replace with your ChatMessage model later
    onSendMessage: (String) -> Unit
) {
    var newMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f), // 60% screen height
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCharcoal)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chat with $inspectorId", color = Color.White, fontSize = 18.sp)
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Messages list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sampleMessages) { msg ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalAlignment = if (msg.sender == "Supervisor") Alignment.End else Alignment.Start
                        ) {
                            Text(
                                text = msg.content,
                                color = Color.White,
                                modifier = Modifier
                                    .background(
                                        if (msg.sender == "Supervisor") SafetyYellow else Color.Gray,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(8.dp)
                            )
                            Text(
                                text = msg.timestamp,
                                fontSize = 10.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }

                // Input field + Send button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newMessage,
                        onValueChange = { newMessage = it },
                        placeholder = {
                            Text(
                                "Write reply...",
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
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
                                    newMessage = ""
                                },
                                enabled = newMessage.isNotBlank()
                            ) {
                                Icon(
                                    Icons.Outlined.Send,
                                    contentDescription = "Send"
                                )
                            }
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newMessage.isNotBlank()) {
                                onSendMessage(newMessage)
                                newMessage = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyYellow)
                    ) {
                        Text("Send", color = Color.Black)
                    }
                }
            }
        }
    }
}

data class ChatMessage(
    val sender: String,   // e.g. "Inspector", "Supervisor"
    val content: String,
    val timestamp: String // just for display, you can use Date later
)

val sampleMessages = listOf(
    ChatMessage("Inspector", "Hello supervisor, I’ve uploaded the inspection report.", "09:00 AM"),
    ChatMessage("Supervisor", "Thanks, I’ll review it shortly.", "09:05 AM"),
    ChatMessage("Inspector", "Please check the electrical section, it might need more details.", "09:07 AM"),
    ChatMessage("Supervisor", "Got it. I’ll request an update if needed.", "09:10 AM"),
    ChatMessage("Inspector", "Okay, waiting for your feedback.", "09:15 AM")
)