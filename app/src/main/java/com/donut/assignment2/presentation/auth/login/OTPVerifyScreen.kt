package com.donut.assignment2.presentation.auth.login

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.donut.assignment2.R
import com.donut.assignment2.domain.model.User
import com.google.firebase.BuildConfig

@Composable
fun OTPVerificationScreen(
    verificationId: String,
    onVerificationSuccess: (User) -> Unit,
    onBackToPhone: () -> Unit,
    viewModel: FirebaseAuthViewModel = hiltViewModel()
) {
    var otpCode by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    // Log for debugging
    LaunchedEffect(verificationId) {
        Log.d("OTPVerificationScreen", "Received verificationId: $verificationId")
    }

    // Handle verification success
    LaunchedEffect(uiState.isVerificationSuccess, uiState.user) {
        if (uiState.isVerificationSuccess && uiState.user != null) {
            Log.d("OTPVerificationScreen", "Verification successful for user: ${uiState.user!!.id}")
            onVerificationSuccess(uiState.user!!)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 180.dp)
                .padding(horizontal = 45.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header logo + text
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Phuong Hai",
                        color = Color.White,
                        fontWeight = FontWeight.W900,
                        fontSize = 26.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "FIELD INSPECTION",
                        color = Color.White,
                        fontWeight = FontWeight.W500,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Title
            Text(
                text = "Enter verification \n code",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))
            Row {
                Text(
                    text = "We sent a 6-digit code to ",
                    fontSize = 16.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "your phone",
                    fontSize = 16.sp,
                    color = Color(0xFFFFD700),
                    style = TextStyle(textDecoration = TextDecoration.Underline)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // OTP input
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = otpCode,
                onValueChange = { newValue ->
                    if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                        otpCode = newValue
                        viewModel.clearError() // Clear error when user types
                    }
                },
                placeholder = { Text("123456") },
                label = { Text("OTP Code") },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = !uiState.errorMessage.isNullOrBlank()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Verify button
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                onClick = {
                    when {
                        verificationId.isBlank() -> {
                            Log.e("OTPVerificationScreen", "VerificationId is blank")
                        }
                        otpCode.length != 6 -> {
                            Log.w("OTPVerificationScreen", "OTP length is ${otpCode.length}, expected 6")
                        }
                        else -> {
                            Log.d("OTPVerificationScreen", "Verifying OTP: $otpCode with ID: $verificationId")
                            viewModel.verifyOTPWithId(verificationId, otpCode)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(10.dp),
                enabled = !uiState.isLoading && otpCode.length == 6 && verificationId.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Verify",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back to phone
            TextButton(onClick = {
                Log.d("OTPVerificationScreen", "Going back to phone input")
                onBackToPhone()
            }) {
                Text(
                    text = "Change phone number?",
                    color = Color(0xFFFFD700),
                    fontSize = 16.sp,
                    style = TextStyle(textDecoration = TextDecoration.Underline)
                )
            }

            // Error display
            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Debug info (remove in production)
                if (BuildConfig.DEBUG) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Debug: VerificationId = ${verificationId.take(10)}...",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Success indicator
            if (uiState.isVerificationSuccess) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Xác thực thành công! Đang chuyển hướng...",
                    color = Color.Green,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}