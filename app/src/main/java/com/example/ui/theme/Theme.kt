package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = MinimalistIndigo,
    secondary = MinimalistEmerald,
    tertiary = AccentPurple,
    background = Color(0xFF0F1021),
    surface = Color(0xFF171936),
    surfaceVariant = Color(0xFF1E2145),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = MinimalistIndigo,
    secondary = MinimalistEmerald,
    tertiary = AccentPurple,
    background = MinimalistBg,
    surface = MinimalistSurface,
    surfaceVariant = MinimalistSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = MinimalistSlate,
    onTertiary = MinimalistSlate,
    onBackground = MinimalistSlate,
    onSurface = MinimalistSlate
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // default to true for premium look
    dynamicColor: Boolean = false, // Use our brand neon colors
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
