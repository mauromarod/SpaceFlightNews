package com.mauromarod.spaceflightnews.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SpaceBlue,
    secondary = SpaceOrange,
    background = SpaceDeepNavy,
    surface = SpaceSurface,
    surfaceVariant = SpaceSurfaceVariant,
    onPrimary = SpaceDeepNavy,
    onSecondary = SpaceDeepNavy,
    onBackground = SpaceOnSurface,
    onSurface = SpaceOnSurface,
    onSurfaceVariant = SpaceOnSurfaceVariant,
    error = SpaceError,
    onError = SpaceOnError,
    outline = SpaceOutline
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = SpaceOrange,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onPrimary = SpaceOnError,
    onSecondary = SpaceOnError,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onSurfaceVariant = SpaceOnSurfaceVariant,
    error = SpaceError,
    onError = SpaceOnError,
    outline = SpaceOutline
)

@Composable
fun SpaceFlightNewsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SpaceFlightNewsTypography,
        shapes = SpaceFlightNewsShapes,
        content = content
    )
}
