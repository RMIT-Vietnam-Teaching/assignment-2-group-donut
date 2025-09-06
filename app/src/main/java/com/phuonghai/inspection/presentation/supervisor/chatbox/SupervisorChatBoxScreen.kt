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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.phuonghai.inspection.presentation.navigation.Screen
import com.phuonghai.inspection.presentation.theme.SafetyYellow
import com.phuonghai.inspection.presentation.theme.DarkCharcoal

data class ChatMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val timestamp: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorChatBoxScreen(
    navController: NavController,
) {
    val inspectors = listOf(
        "inspector001" to "Nguyễn Đình Lâm",
        "inspector002" to "Trần Thanh Lâm",
        "inspector003" to "Vu Tuan"
    )

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(inspectors.size) { index ->
                val (id, name) = inspectors[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("${Screen.SupervisorChatDetailScreen.route}/$id")
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circle Avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SafetyYellow, shape = RoundedCornerShape(50))
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(name, color = Color.White, fontSize = 18.sp)
                }
            }
        }

    }
}
