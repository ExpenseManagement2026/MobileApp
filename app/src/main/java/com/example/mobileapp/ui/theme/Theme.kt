package com.example.mobileapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF26A480),
    secondary = Color(0xFF2ECC9A),
    background = Color(0xFFF5F5F5),
    surface = Color.White
)

@Composable
fun MobileAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
