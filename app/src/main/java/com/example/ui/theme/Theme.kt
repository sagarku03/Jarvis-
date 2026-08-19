package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SophisticatedDarkColorScheme = darkColorScheme(
    primary = SophisticatedPrimary,
    onPrimary = SophisticatedTertiary,
    primaryContainer = SophisticatedPrimaryContainer,
    onPrimaryContainer = SophisticatedOnPrimaryContainer,
    secondary = SophisticatedSecondary,
    onSecondary = SophisticatedOnSecondary,
    secondaryContainer = SophisticatedTertiary,
    onSecondaryContainer = SophisticatedSecondary,
    tertiary = SophisticatedAccentGreen,
    onTertiary = Color(0xFF0F3814),
    background = SophisticatedBgDark,
    onBackground = SophisticatedTextPrimary,
    surface = SophisticatedSurfaceDark,
    onSurface = SophisticatedTextPrimary,
    surfaceVariant = SophisticatedSurfaceVariant,
    onSurfaceVariant = SophisticatedTextSecondary,
    outline = SophisticatedCardBorder,
    error = SophisticatedAccentRed,
    onError = Color(0xFF601410)
)

private val SophisticatedLightColorScheme = lightColorScheme(
    primary = SophisticatedTertiary,
    onPrimary = Color.White,
    primaryContainer = SophisticatedSecondary,
    onPrimaryContainer = SophisticatedTertiary,
    secondary = SophisticatedPrimaryContainer,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = SophisticatedTertiary,
    background = Color(0xFFFDF8FD),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    error = SophisticatedAccentRed,
    onError = Color.White
)

@Composable
fun JarvisTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SophisticatedDarkColorScheme else SophisticatedLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
