package com.phuonghai.inspection.presentation.home.inspector

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast
import com.phuonghai.inspection.R
import com.phuonghai.inspection.presentation.generalUI.ButtonUI
import com.phuonghai.inspection.presentation.navigation.Screen
import com.phuonghai.inspection.presentation.home.inspector.profile.InspectorProfileViewModel

@Composable
fun InspectorProfileScreen(modifier: Modifier = Modifier, navController: NavController) {
    var isChatBoxOpen by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val viewModel: InspectorProfileViewModel = hiltViewModel()
    val userState by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val signOutSuccess by viewModel.signOutSuccess.collectAsState() // ✅ ADD THIS

    var message by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadUser()
    }

    LaunchedEffect(signOutSuccess) {
        if (signOutSuccess) {
            Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
            viewModel.clearSignOutSuccess()

            navController.navigate(Screen.LoginScreen.route) {
                popUpTo(Screen.SplashScreen.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    fun handleSignOut() {
        viewModel.signOut()
    }

    if(isLoading){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }else {
        val fullName = userState?.fullName ?: ""
        val email = userState?.email ?: ""
        val contact = userState?.phoneNumber ?: ""
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(horizontal = 20.dp),
        ) {
            Text(
                text = "Account",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 50.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(30.dp)
                        .height(350.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.account),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(140.dp)
                    )
                    Text(
                        text = fullName,
                        color = Color.Black,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "Email: ",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = email,
                            color = Color.Gray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "Contact: ",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            contact,
                            color = Color.Gray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ✅ SIMPLE LOGOUT BUTTON - EXACT SAME PATTERN AS PETPAL
                        ButtonUI(
                            text = "Logout",
                            onClick = { showLogoutDialog = true },
                            icon = Icons.AutoMirrored.Outlined.Logout,
                            modifier = modifier.clip(RoundedCornerShape(50.dp))
                        )
                        ButtonUI(
                            text = "Chat Box",
                            onClick = { isChatBoxOpen = true },
                            icon = Icons.Outlined.Chat,
                            modifier = modifier.clip(RoundedCornerShape(50.dp))
                        )
                    }
                }

                // ✅ SIMPLE LOGOUT CONFIRMATION DIALOG
                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = {
                            Text(
                                text = "Sign Out",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Text(
                                text = "Are you sure you want to sign out?",
                                fontSize = 16.sp
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showLogoutDialog = false
                                    handleSignOut() // ✅ CALL SIMPLE SIGN OUT FUNCTION
                                }
                            ) {
                                Text(
                                    "Sign Out",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLogoutDialog = false }) {
                                Text("Cancel")
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = Color.White
                    )
                }

                // Chat Dialog (existing code remains same)
                if (isChatBoxOpen) {
                    AlertDialog(
                        onDismissRequest = { isChatBoxOpen = false },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { isChatBoxOpen = false }) {
                                Text("Close", color = MaterialTheme.colorScheme.primary)
                            }
                        },
                        title = {
                            Text(
                                text = "🗨️ Chat Support",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(500.dp)
                                    .padding(top = 8.dp)
                                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF6F6F6))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "Welcome to the chat! How can I assist you today?",
                                        color = Color.DarkGray,
                                        fontSize = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = message,
                                        onValueChange = { message = it },
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
                                            IconButton(onClick = {
                                                message = ""
                                            }) {
                                                Icon(
                                                    Icons.Outlined.Send,
                                                    contentDescription = "Send"
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        containerColor = Color.White
                    )
                }
            }
        }
    }
}