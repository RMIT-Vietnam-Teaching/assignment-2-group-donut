package com.phuonghai.inspection.presentation.auth

import android.app.Activity
import android.content.ContextWrapper
import android.widget.Toast
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
import androidx.navigation.NavController
import com.phuonghai.inspection.R
import com.phuonghai.inspection.domain.repository.AuthState
import com.phuonghai.inspection.presentation.auth.login.PhoneLoginViewModel

@Composable
fun PhoneLoginScreen(navController: NavController) {
    val viewModel: PhoneLoginViewModel = hiltViewModel()
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val activity = rememberActivityOrNull()

    LaunchedEffect(Unit) {
        viewModel.authState.collect { state ->
            when (state) {
                is AuthState.Loading -> {
                    isLoading = true
                    localError = null
                }
                is AuthState.CodeSent -> {
                    isLoading = false
                    navController.navigate("otp_screen/${state.verificationId}")
                }
                is AuthState.Error -> {
                    isLoading = false
                    localError = state.message
                }
                is AuthState.Success -> {
                    isLoading = false
                }
                is AuthState.CodeTimeout -> {
                    isLoading = false
                    localError = "Hết hạn OTP, gửi lại mã."
                }
            }
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
            // Logo và Title
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

            // Main Title
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

            // Phone Input Field
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = phoneNumber,
                onValueChange = { newValue ->
                    if (newValue.all { ch -> ch.isDigit() || ch in "+()-. " }) {
                        phoneNumber = newValue
                        localError = null
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
                isError = !localError.isNullOrBlank()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Send OTP Button
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                onClick = {
                    localError = null

                    when {
                        activity == null -> {
                            localError = "Không thể lấy được Activity để gửi OTP."
                        }
                        phoneNumber.isBlank() -> {
                            localError = "Vui lòng nhập số điện thoại"
                        }
                        phoneNumber.length < 9 -> {
                            localError = "Số điện thoại không hợp lệ"
                        }
                        else -> {
                            val formattedPhone = if (!phoneNumber.startsWith("+84")) {
                                "+84${phoneNumber.removePrefix("0")}"
                            } else {
                                phoneNumber
                            }
                            viewModel.sendVerificationCode(formattedPhone, activity)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(10.dp),
                enabled = !isLoading && phoneNumber.isNotBlank()
            ) {
                if (isLoading) {
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

            // Error Message
            if (!localError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = localError!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

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