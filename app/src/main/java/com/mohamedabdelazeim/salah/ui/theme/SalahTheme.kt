package com.mohamedabdelazeim.salah.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Gold = Color(0xFFFFD700)
private val DarkGreen = Color(0xFF1B5E20)

private val DarkColors = darkColorScheme(
    primary = Gold,
    onPrimary = Color.Black,
    secondary = DarkGreen,
    background = Color(0xFF0D1B0F),
    surface = Color(0xFF1A2E1C),
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun SalahTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
