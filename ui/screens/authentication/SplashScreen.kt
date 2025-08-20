package com.example.ui_for_assignment2.ui.screens.authentication


import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkCharcoal = Color(0xFF1E1E1E)
private val OffWhite = Color(0xFFFAFAFA)
private val SafetyYellow = Color(0xFFFFD700)
private val StatusGreen = Color(0xFF4CAF50)
private val StatusRed = Color(0xFFE53935)
private val StatusOrange = Color(0xFFFB8C00)

@Composable
fun SplashScreen() {
    Scaffold(
        containerColor = DarkCharcoal
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Inspector App",
                    color = SafetyYellow,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Loading…",
                    color = OffWhite.copy(alpha = 0.85f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(4.dp),
                    color = SafetyYellow,
                    trackColor = OffWhite.copy(alpha = 0.15f)
                )
            }
        }
    }
}

@Preview(
    name = "Splash - Dark",
    showBackground = true,
    backgroundColor = 0xFF1E1E1E
)
@Composable
private fun PreviewSplashScreenDark() {
    MaterialTheme { SplashScreen() }
}
