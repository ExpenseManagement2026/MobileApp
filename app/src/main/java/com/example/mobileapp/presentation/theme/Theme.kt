package com.example.mobileapp.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary        = Color(0xFF2DC98E),
    secondary      = Color(0xFF26A480),
    background     = Color(0xFFF5F5F5),
    surface        = Color(0xFFFFFFFF),
    onPrimary      = Color.White,
    onBackground   = Color(0xFF1A1A1A),
    onSurface      = Color(0xFF1A1A1A),
)

private val DarkColorScheme = darkColorScheme(
    primary        = Color(0xFF2DC98E),
    secondary      = Color(0xFF26A480),
    background     = Color(0xFF121212),
    surface        = Color(0xFF1E1E1E),
    onPrimary      = Color.White,
    onBackground   = Color(0xFFE0E0E0),
    onSurface      = Color(0xFFE0E0E0),
)

@Composable
fun MobileAppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content,
    )
}
