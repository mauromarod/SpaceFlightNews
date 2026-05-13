package com.mauromarod.spaceflightnews.core.uicomponents

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.mauromarod.spaceflightnews.core.designsystem.VergeTileElectricBlue

@Composable
fun NetworkImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var state: AsyncImagePainter.State by remember { mutableStateOf(AsyncImagePainter.State.Empty) }

    Box(modifier = modifier) {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.small)
                .testTag(NetworkImageTags.IMAGE),
            onState = { state = it }
        )
        when (state) {
            is AsyncImagePainter.State.Success -> Unit
            is AsyncImagePainter.State.Error -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            )

            else -> ShimmerBox(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(16f / 9f)
            )
        }
    }
}

@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(animation = tween(800)),
        label = "shimmerAlpha"
    )
    Box(
        modifier = modifier
            .alpha(alpha)
            .background(MaterialTheme.colorScheme.primaryContainer)
    )
}

object NetworkImageTags {
    const val IMAGE = "network_image"
}
