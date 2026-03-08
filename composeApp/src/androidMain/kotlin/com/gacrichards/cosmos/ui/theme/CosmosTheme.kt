package com.gacrichards.cosmos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CosmosColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF003175),
    primaryContainer = Color(0xFF004399),
    onPrimaryContainer = Color(0xFFD6E3FF),
    background = Color(0xFF080D17),
    onBackground = Color(0xFFDDE3F2),
    surface = Color(0xFF0D1424),
    onSurface = Color(0xFFDDE3F2),
    surfaceVariant = Color(0xFF1A2540),
    onSurfaceVariant = Color(0xFFA9B4CF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun CosmosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CosmosColorScheme,
        content = content,
    )
}
