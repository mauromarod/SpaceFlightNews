package com.mauromarod.spaceflightnews.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val VergeDarkColorScheme = darkColorScheme(
    primary = VergeJellyMint,
    onPrimary = VergeAbsoluteBlack,
    primaryContainer = VergeSurfaceSlate,
    onPrimaryContainer = VergeJellyMint,
    secondary = VergeUltraviolet,
    onSecondary = VergeHazardWhite,
    background = VergeCanvas,
    onBackground = VergeHazardWhite,
    surface = VergeSurfaceSlate,
    onSurface = VergeHazardWhite,
    surfaceVariant = VergeCanvas,
    onSurfaceVariant = VergeSecondaryText,
    outline = VergeHazardWhite,
    outlineVariant = VergeConsoleMintBorder,
    error = VergeTileOrange,
    onError = VergeHazardWhite,
    inverseSurface = VergeHazardWhite,
    inverseOnSurface = VergeCanvas,
    inversePrimary = VergeDeepLinkBlue,
)

private val VergeLightColorScheme = lightColorScheme(
    primary = VergeJellyMint,
    onPrimary = VergeAbsoluteBlack,
    primaryContainer = VergeLightSurfaceVariant,
    onPrimaryContainer = VergeCanvas,
    secondary = VergeUltraviolet,
    onSecondary = VergeHazardWhite,
    background = VergeLightBackground,
    onBackground = VergeCanvas,
    surface = VergeLightSurface,
    onSurface = VergeCanvas,
    surfaceVariant = VergeLightSurfaceVariant,
    onSurfaceVariant = VergeLightSecondaryText,
    outline = VergeCanvas,
    outlineVariant = VergeConsoleMintBorder,
    error = VergeTileOrange,
    onError = VergeHazardWhite,
    inverseSurface = VergeCanvas,
    inverseOnSurface = VergeLightBackground,
    inversePrimary = VergeDeepLinkBlue,
)

@Composable
fun SpaceFlightNewsTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) VergeDarkColorScheme else VergeLightColorScheme
    CompositionLocalProvider(LocalVergeSpacing provides VergeSpacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = VergeTypography,
            shapes = VergeShapes,
            content = content,
        )
    }
}
