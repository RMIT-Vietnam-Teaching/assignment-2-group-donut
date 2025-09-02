package com.phuonghai.inspection.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ✅ Updated Dark Color Scheme using your defined colors
private val DarkColorScheme = darkColorScheme(
    primary = SafetyYellow,
    secondary = SafetyOrange,
    tertiary = StatusBlue,
    background = Color.Black, // Pure black background
    surface = SurfaceDark,
    surfaceVariant = SurfaceDarkHigh,
    onPrimary = Color.Black, // Black text on yellow background
    onSecondary = OffWhite,
    onTertiary = OffWhite,
    onBackground = OffWhite,
    onSurface = OffWhite,
    onSurfaceVariant = TextLight,
    error = SafetyRed,
    onError = OffWhite,
    outline = BorderDark,
    outlineVariant = TextSecondary,
    scrim = Color.Black.copy(alpha = 0.8f),
    inverseSurface = OffWhite,
    inverseOnSurface = Color.Black,
    inversePrimary = BrightPrimary,
    surfaceTint = SafetyYellow
)

// ✅ Updated Light Color Scheme using your defined colors
private val LightColorScheme = lightColorScheme(
    primary = BrightPrimary,
    secondary = BrightSecondary,
    tertiary = StatusBlue,
    background = BrightBackground,
    surface = OffWhite,
    surfaceVariant = Color(0xFFF5F5F5),
    onPrimary = OffWhite,
    onSecondary = OffWhite,
    onTertiary = OffWhite,
    onBackground = DarkText,
    onSurface = DarkText,
    onSurfaceVariant = TextSecondary,
    error = BrightRed,
    onError = OffWhite,
    outline = Color(0xFFCACACA),
    outlineVariant = Color(0xFFE0E0E0),
    scrim = Color.Black.copy(alpha = 0.6f),
    inverseSurface = DarkCharcoal,
    inverseOnSurface = OffWhite,
    inversePrimary = SafetyYellow,
    surfaceTint = BrightPrimary
)

@Composable
fun FieldInspectionTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}