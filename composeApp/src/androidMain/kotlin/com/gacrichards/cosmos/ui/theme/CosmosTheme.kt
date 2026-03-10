package com.gacrichards.cosmos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val CosmosColorScheme = darkColorScheme(
    primary = CosmosBlue,
    onPrimary = CosmosOnBlue,
    primaryContainer = CosmosBlueContainer,
    onPrimaryContainer = CosmosOnBlueContainer,
    background = CosmosBackground,
    onBackground = CosmosOnBackground,
    surface = CosmosSurface,
    onSurface = CosmosOnSurface,
    surfaceVariant = CosmosSurfaceVariant,
    onSurfaceVariant = CosmosOnSurfaceVariant,
    error = CosmosError,
    onError = CosmosOnError,
    errorContainer = CosmosErrorContainer,
    onErrorContainer = CosmosOnErrorContainer,
)

@Composable
fun CosmosTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalCosmosSpacing provides CosmosSpacing()) {
        MaterialTheme(
            colorScheme = CosmosColorScheme,
            typography = CosmosTypography,
            content = content,
        )
    }
}
