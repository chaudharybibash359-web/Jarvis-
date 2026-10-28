package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = JarvisBackground,
    primaryContainer = JarvisSurfaceVariant,
    onPrimaryContainer = JarvisCyanLight,
    secondary = JarvisNeonBlue,
    onSecondary = JarvisBackground,
    secondaryContainer = JarvisSurfaceVariant,
    onSecondaryContainer = JarvisTextPrimary,
    tertiary = JarvisAccentGreen,
    onTertiary = JarvisBackground,
    background = JarvisBackground,
    onBackground = JarvisTextPrimary,
    surface = JarvisSurface,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisSurfaceVariant,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisCyanDim,
    outlineVariant = JarvisBorderSubtle,
    error = JarvisAccentRed,
    onError = JarvisTextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}

