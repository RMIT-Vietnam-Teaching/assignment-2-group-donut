package com.example.a2_adrian.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary   = SafetyYellow,      // nút vàng
    onPrimary = Color.Black,       // chữ đen trên vàng
    secondary = StatusGreen,
    error     = StatusRed,
    background = BgDark,
    surface    = SurfaceDark,
    surfaceVariant = SurfaceDarkHigh,
    onSurface  = OffWhite,
    outline    = BorderDark,
)

@Composable
fun A2AdrianTheme(
    // Ép dark để giống ảnh; nếu muốn theo hệ thống, đổi lại = isSystemInDarkTheme()
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
