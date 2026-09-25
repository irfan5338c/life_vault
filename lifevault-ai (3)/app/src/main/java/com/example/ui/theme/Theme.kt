package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BentoIndigoLight,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = BentoIndigoContainer,
    onPrimaryContainer = BentoIndigoLight,
    secondary = BentoAmberLight,
    onSecondary = Color(0xFF1E1B4B),
    secondaryContainer = BentoAmberContainer,
    onSecondaryContainer = BentoAmberLight,
    tertiary = BentoEmeraldLight,
    onTertiary = Color(0xFF064E3B),
    tertiaryContainer = BentoEmeraldContainer,
    onTertiaryContainer = BentoEmeraldLight,
    background = BentoBackgroundDark,
    onBackground = BentoTextPrimaryDark,
    surface = BentoSurfaceDark,
    onSurface = BentoTextPrimaryDark,
    surfaceVariant = BentoCardDark,
    onSurfaceVariant = BentoTextSecondaryDark,
    outline = BentoBorderDark,
    outlineVariant = BentoBorderSubtleDark
)

private val LightColorScheme = lightColorScheme(
    primary = BentoIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = BentoIndigo,
    secondary = BentoAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF92400E),
    tertiary = BentoEmerald,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    background = BentoBackgroundLight,
    onBackground = BentoTextPrimaryLight,
    surface = BentoSurfaceLight,
    onSurface = BentoTextPrimaryLight,
    surfaceVariant = BentoCardElevatedLight,
    onSurfaceVariant = BentoTextSecondaryLight,
    outline = BentoBorderLight,
    outlineVariant = BentoBorderSubtleLight
)

@Composable
fun LifeVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backwards compatibility for existing references
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    LifeVaultTheme(darkTheme = darkTheme, content = content)
}
