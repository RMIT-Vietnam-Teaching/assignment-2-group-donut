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
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import coil.compose.rememberAsyncImagePainter
import com.phuonghai.inspection.R
import com.phuonghai.inspection.presentation.generalUI.ButtonUI
import com.phuonghai.inspection.presentation.navigation.Screen
import com.phuonghai.inspection.presentation.home.inspector.profile.InspectorProfileViewModel

@Composable
fun InspectorProfileScreen(modifier: Modifier = Modifier, navController: NavController) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    val viewModel: InspectorProfileViewModel = hiltViewModel()
    val userState by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val supervisorName by viewModel.supervisorName.collectAsState()
    val signOutSuccess by viewModel.signOutSuccess.collectAsState() // ✅ ADD THIS

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
        val role = userState?.role?.name ?: ""
        val profileImageUrl = userState?.profileImageUrl ?: ""
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
                        .height(420.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Image(
                        painter = if (profileImageUrl.isNotBlank()) {
                            rememberAsyncImagePainter(profileImageUrl)
                        } else {
                            painterResource(id = R.drawable.account)
                        },
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape) // 👌 round avatar style
                            .border(2.dp, Color.Gray, CircleShape)
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "Role: ",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            role,
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
                            text = "Boss: ",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            supervisorName,
                            color = Color.Gray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
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
            }
        }
    }
}