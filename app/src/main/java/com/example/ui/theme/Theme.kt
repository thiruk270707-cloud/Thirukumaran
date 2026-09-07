package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MomCareColorScheme = darkColorScheme(
    primary = Rose500,
    onPrimary = Color.White,
    primaryContainer = Rose900,
    onPrimaryContainer = Rose400,
    secondary = Pink500,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF701A75),
    onSecondaryContainer = Color(0xFFF472B6),
    tertiary = Cyan400,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = Color(0xFFEF4444),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MomCareColorScheme,
        typography = Typography,
        content = content
    )
}
