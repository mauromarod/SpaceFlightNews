package com.mauromarod.spaceflightnews.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class VergeSpacing(
    val micro: Dp = 2.dp,
    val xSmall: Dp = 4.dp,
    val tiny: Dp = 6.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val xLarge: Dp = 32.dp,
    val xxLarge: Dp = 48.dp,
    val section: Dp = 64.dp,
    val cells: Dp = 128.dp,
)

val LocalVergeSpacing = compositionLocalOf { VergeSpacing() }

val MaterialTheme.spacing: VergeSpacing
    @Composable
    @ReadOnlyComposable
    get() = LocalVergeSpacing.current
