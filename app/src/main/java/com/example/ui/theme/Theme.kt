package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ButtonYellow,
    onPrimary = OnButtonYellow,
    primaryContainer = ButtonYellowDark,
    onPrimaryContainer = Color.White,
    secondary = Emerald500,
    onSecondary = Color.White,
    secondaryContainer = Emerald700,
    onSecondaryContainer = Emerald100,
    tertiary = Amber500,
    onTertiary = Color.Black,
    tertiaryContainer = Amber600,
    onTertiaryContainer = Amber100,
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate300,
    error = Rose500,
    onError = Color.White,
    outline = Slate700,
    outlineVariant = Slate800
)

private val LightColorScheme = lightColorScheme(
    primary = ButtonYellow,
    onPrimary = OnButtonYellow,
    primaryContainer = ButtonYellowContainer,
    onPrimaryContainer = OnButtonYellow,
    secondary = Emerald600,
    onSecondary = Color.White,
    secondaryContainer = Emerald100,
    onSecondaryContainer = Emerald700,
    tertiary = Amber600,
    onTertiary = Color.White,
    tertiaryContainer = Amber100,
    onTertiaryContainer = Amber600,
    background = Color(0xFFF8FAFC), // Slate50
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFF1F5F9), // Slate100
    onSurfaceVariant = Slate600,
    error = Rose500,
    onError = Color.White,
    outline = Slate300,
    outlineVariant = Slate200
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our tailored high-trust palette by default
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
