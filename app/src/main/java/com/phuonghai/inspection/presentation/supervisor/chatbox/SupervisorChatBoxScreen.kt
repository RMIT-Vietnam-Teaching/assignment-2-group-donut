package com.phuonghai.inspection.presentation.supervisor.chatbox

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.phuonghai.inspection.presentation.navigation.Screen
import com.phuonghai.inspection.presentation.theme.SafetyYellow
import com.phuonghai.inspection.presentation.theme.DarkCharcoal


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorChatBoxScreen(
    navController: NavController,
    viewModel: SupervisorChatViewModel = hiltViewModel()

) {


    val chatHistoryState by viewModel.listChatHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Text ("Chat with Inspector", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }, colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCharcoal)
            )
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        if (isLoading) {
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
                    .padding(innerPadding)
            ) {
                val chatHistory = chatHistoryState
                items(chatHistory.size) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("${Screen.SupervisorChatDetailScreen.route}/${chatHistory[it].inspectorId}/${chatHistory[it].inspectorName}")
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(SafetyYellow, shape = RoundedCornerShape(50))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                chatHistory[it].inspectorName?.let { name ->
                                    val phone = chatHistory[it].inspectorPhone ?: "Unknown"
                                    Text(
                                        text = "$name ($phone)",
                                        fontSize = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Text(
                                text = chatHistory[it].latestMessage?.text ?: "No messages yet",
                                maxLines = 2, // limit to 2 lines
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
