package com.phuonghai.inspection.presentation.auth

import android.widget.Toast
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.phuonghai.inspection.R
import com.phuonghai.inspection.domain.model.UserRole
import com.phuonghai.inspection.domain.repository.AuthState
import com.phuonghai.inspection.presentation.auth.login.OTPVerificationViewModel

@Composable
fun OtpScreen(
    navController: NavController,
    verificationId: String,
    viewModel: OTPVerificationViewModel = hiltViewModel()
) {
    val ctx = LocalContext.current
    var otpCode by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    // State từ ViewModel
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val lastMessage by viewModel.lastMessage.collectAsStateWithLifecycle()
    val role by viewModel.userRole.collectAsStateWithLifecycle()

    // Nhận các event 1-shot từ repo
    LaunchedEffect(Unit) {
        viewModel.authState.collect { st ->
            when (st) {
                is AuthState.Error -> {
                    localError = st.message
                    Toast.makeText(ctx, "Lỗi: ${st.message}", Toast.LENGTH_SHORT).show()
                }
                is AuthState.CodeTimeout -> {
                    localError = "Hết hạn OTP, gửi lại mã."
                    Toast.makeText(ctx, "Hết hạn OTP, gửi lại mã.", Toast.LENGTH_SHORT).show()
                }
                is AuthState.CodeSent -> {
                    Toast.makeText(ctx, "Đã gửi mã OTP.", Toast.LENGTH_SHORT).show()
                }
                is AuthState.Success -> {
                    Toast.makeText(ctx, "Login successful.", Toast.LENGTH_SHORT).show()
                    localError = null
                }
                is AuthState.Loading -> {
                    localError = null
                }
            }
        }
    }

    // Điều hướng khi đã có role
    LaunchedEffect(role) {
        when (role) {
            UserRole.INSPECTOR -> {
                navController.navigate("inspector_dashboard") {
                    popUpTo("login_screen") { inclusive = true }
                }
            }
            UserRole.SUPERVISOR -> {
                navController.navigate("supervisor_dashboard") {
                    popUpTo("login_screen") { inclusive = true }
                }
            }
            null -> {} // chờ
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
                text = "Enter verification \n code",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Subtitle
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

            // OTP Input Field
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = otpCode,
                onValueChange = { newValue ->
                    if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                        otpCode = newValue
                        localError = null
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
                isError = !localError.isNullOrBlank()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Verify Button
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                onClick = {
                    localError = null
                    when {
                        verificationId.isBlank() -> {
                            localError = "Verification ID not "
                        }
                        otpCode.length != 6 -> {
                            localError = "please input enough 6 number"
                        }
                        else -> {
                            viewModel.verifyCode(verificationId, otpCode)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(10.dp),
                enabled = !isLoading && otpCode.length == 6 && verificationId.isNotBlank()
            ) {
                if (isLoading) {
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

            // Back to Phone Button
            TextButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Text(
                    text = "Change phone number?",
                    color = Color(0xFFFFD700),
                    fontSize = 16.sp,
                    style = TextStyle(textDecoration = TextDecoration.Underline)
                )
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

            // Debug Info (chỉ hiện trong development)
            if (com.google.firebase.BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Debug: loading=$isLoading | msg='$lastMessage' | role=$role",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}