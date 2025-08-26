package com.donut.assignment2.presentation.auth.login

import android.content.ContextWrapper
import android.util.Log
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun PhoneInputScreen(
    onOTPSent: (String) -> Unit,
    viewModel: FirebaseAuthViewModel = hiltViewModel()
) {
    var phoneNumber by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val uiState by viewModel.uiState.collectAsState()

    // Handle OTP sent navigation - với kiểm tra an toàn
    LaunchedEffect(uiState.otpSent, uiState.verificationId) {
        if (uiState.otpSent && !uiState.verificationId.isNullOrBlank()) {
            Log.d("PhoneInputScreen", "Navigating with verificationId: ${uiState.verificationId}")
            onOTPSent(uiState.verificationId!!)
        } else if (uiState.otpSent && uiState.verificationId.isNullOrBlank()) {
            Log.w("PhoneInputScreen", "OTP sent but verificationId is null/blank")
            localError = "Lỗi hệ thống: Không nhận được ID xác thực"
        }
    }

    // Handle auto verification
    LaunchedEffect(uiState.isVerificationSuccess, uiState.user) {
        if (uiState.isVerificationSuccess && uiState.user != null) {
            Log.d("PhoneInputScreen", "Auto verification successful")
            // Có thể navigate trực tiếp đến dashboard ở đây
        }
    }

    val activity = rememberActivityOrNull()

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
            // Logo section
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
                text = "Enter your phone \n number",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Subtitle
            Row {
                Text(
                    text = "We'll send OTP to verify ",
                    fontSize = 16.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "your number",
                    fontSize = 16.sp,
                    color = Color(0xFFFFD700),
                    style = TextStyle(textDecoration = TextDecoration.Underline)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Phone input
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = phoneNumber,
                onValueChange = { newValue ->
                    // Chỉ cho phép số và các ký tự phone hợp lệ
                    if (newValue.all { ch -> ch.isDigit() || ch in "+()-. " }) {
                        phoneNumber = newValue
                        localError = null // Clear local error khi user nhập
                        viewModel.clearError() // Clear ViewModel error
                    }
                },
                placeholder = { Text("0987654321") },
                label = { Text("Phone Number") },
                prefix = { Text("+84 ", color = Color.Gray) },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    errorTextColor = Color.White,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                isError = !uiState.errorMessage.isNullOrBlank() || !localError.isNullOrBlank()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Send OTP button
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                onClick = {
                    localError = null
                    viewModel.clearError()

                    when {
                        activity == null -> {
                            localError = "Không thể lấy được Activity để gửi OTP."
                            Log.e("PhoneInputScreen", "Activity is null")
                        }
                        phoneNumber.isBlank() -> {
                            localError = "Vui lòng nhập số điện thoại"
                        }
                        phoneNumber.length < 9 -> {
                            localError = "Số điện thoại không hợp lệ"
                        }
                        else -> {
                            Log.d("PhoneInputScreen", "Sending OTP to: $phoneNumber")
                            viewModel.sendOTP(phoneNumber, activity)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(10.dp),
                enabled = !uiState.isLoading && phoneNumber.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Send OTP",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Error display - ưu tiên error từ ViewModel
            val errorText = uiState.errorMessage ?: localError
            if (!errorText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorText,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Success message khi OTP đã gửi
            if (uiState.otpSent && uiState.verificationId != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "OTP đã được gửi thành công!",
                    color = Color.Green,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** Lấy ComponentActivity an toàn từ LocalContext */
@Composable
private fun rememberActivityOrNull(): ComponentActivity? {
    val context = LocalContext.current
    return remember(context) {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is ComponentActivity) return@remember ctx
            ctx = ctx.baseContext
        }
        null
    }
}