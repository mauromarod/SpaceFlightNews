package com.mauromarod.spaceflightnews.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val VergeShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),   // inputs, badges — typewriter tag feel
    small = RoundedCornerShape(4.dp),         // inline images, nested cards
    medium = RoundedCornerShape(20.dp),       // standard pill cards, color-block tiles
    large = RoundedCornerShape(24.dp),        // feature cards, primary button pill
    extraLarge = RoundedCornerShape(40.dp),   // outlined CTA pills
)
