package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LuminaLightColorScheme = lightColorScheme(
    primary = Brand700,
    onPrimary = Color.White,
    primaryContainer = Brand100,
    onPrimaryContainer = Brand900,
    secondary = Gold500,
    onSecondary = TextOnGold,
    secondaryContainer = Gold100,
    onSecondaryContainer = TextOnGold,
    tertiary = Brand500,
    onTertiary = Color.White,
    background = BgCanvas,
    onBackground = TextPrimary,
    surface = BgSurface,
    onSurface = TextPrimary,
    surfaceVariant = BgSunken,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFFD3DCD5),
    error = DangerRed,
    onError = Color.White
)

private val LuminaDarkColorScheme = darkColorScheme(
    primary = Brand500,
    onPrimary = Color.White,
    primaryContainer = Brand900,
    onPrimaryContainer = Brand100,
    secondary = Gold500,
    onSecondary = TextOnGold,
    secondaryContainer = Color(0xFF332700),
    onSecondaryContainer = Gold100,
    tertiary = Brand100,
    onTertiary = Brand900,
    background = Color(0xFF031C16),
    onBackground = Color(0xFFE4ECE7),
    surface = Color(0xFF052B22),
    onSurface = Color(0xFFE4ECE7),
    surfaceVariant = Color(0xFF07382D),
    onSurfaceVariant = Color(0xFFA5B7AF),
    outline = Color(0xFF135041),
    error = DangerRed,
    onError = Color.White
)

@Composable
fun LuminaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LuminaDarkColorScheme else LuminaLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Alias for backwards compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    LuminaTheme(darkTheme = darkTheme, content = content)
}

