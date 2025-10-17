package com.example.crimewatch.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object CrimeWatchColors {
    val BackgroundLight = Color(0xFFF6F8FA)
    val BackgroundDark = Color(0xFF121212)
    val AccentRed = Color(0xFFE5534B)
    val AccentDark = Color(0xFF24292F)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val BorderGray = Color(0xFFE1E4E8)
    val TextPrimary = Color(0xFF24292F)
    val TextSecondary = Color(0xFF586069)
}

private val LightColorScheme = lightColorScheme(
    primary = CrimeWatchColors.AccentDark,
    secondary = CrimeWatchColors.AccentRed,
    background = CrimeWatchColors.BackgroundLight,
    surface = CrimeWatchColors.SurfaceWhite,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = CrimeWatchColors.TextPrimary,
    onSurface = CrimeWatchColors.TextPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = CrimeWatchColors.AccentDark,
    secondary = CrimeWatchColors.AccentRed,
    background = CrimeWatchColors.BackgroundDark,
    surface = CrimeWatchColors.AccentDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun CrimeWatchTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(
            titleLarge = MaterialTheme.typography.titleLarge.copy(
                color = colors.onBackground
            ),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(
                color = colors.onBackground
            )
        ),
        content = content
    )
}
