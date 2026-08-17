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
    primary = BlissPrimary,
    onPrimary = Color.White,
    primaryContainer = BlissPrimaryContainer,
    onPrimaryContainer = BlissOnPrimaryContainer,
    secondary = BlissSecondaryDark,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = BlissSecondaryContainer,
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = BlissAccent,
    background = BlissDarkBackground,
    onBackground = BlissDarkText,
    surface = BlissDarkSurface,
    onSurface = BlissDarkText,
    surfaceVariant = BlissDarkSurfaceVariant,
    onSurfaceVariant = BlissDarkTextDim,
    outline = BlissDarkBorder,
    error = BlissError
)

private val LightColorScheme = lightColorScheme(
    primary = BlissPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = BlissSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0F9FF),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = BlissAccent,
    background = BlissLightBackground,
    onBackground = BlissLightText,
    surface = BlissLightSurface,
    onSurface = BlissLightText,
    surfaceVariant = BlissLightSurfaceVariant,
    onSurfaceVariant = BlissLightTextDim,
    outline = BlissLightBorder,
    error = BlissError
)

@Composable
fun BlissOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // prioritize distinct custom theme
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
