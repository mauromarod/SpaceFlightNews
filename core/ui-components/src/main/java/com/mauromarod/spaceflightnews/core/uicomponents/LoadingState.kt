package com.mauromarod.spaceflightnews.core.uicomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.mauromarod.spaceflightnews.core.designsystem.SpaceFlightNewsTheme
import com.mauromarod.spaceflightnews.core.designsystem.spacing

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    val loadingDesc = stringResource(R.string.cd_loading)
    LazyVerticalStaggeredGrid(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = loadingDesc }
            .testTag(LoadingStateTags.CONTAINER),
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(MaterialTheme.spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        verticalItemSpacing = MaterialTheme.spacing.small,
    ) {
        items(8) {
            ArticleCardSkeleton()
        }
    }
}

@Composable
private fun ArticleCardSkeleton() {
    val skeletonHeight = remember { (64..256).random() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        ShimmerBox(
            modifier = Modifier
                .height(skeletonHeight.dp)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .clip(MaterialTheme.shapes.small)
        )

        ShimmerBox(modifier = Modifier
            .fillMaxWidth(0.3f)
            .height(10.dp)
            .clip(MaterialTheme.shapes.extraSmall))
        ShimmerBox(modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .clip(MaterialTheme.shapes.extraSmall))
        ShimmerBox(modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(20.dp)
            .clip(MaterialTheme.shapes.extraSmall))
        ShimmerBox(modifier = Modifier
            .fillMaxWidth(0.4f)
            .height(12.dp)
            .clip(MaterialTheme.shapes.extraSmall))
    }
}


object LoadingStateTags {
    const val CONTAINER = "loading_state"
}

@Preview(name = "LoadingState", showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun LoadingStatePreview() {
    SpaceFlightNewsTheme {
        Box(modifier = Modifier.height(500.dp)) {
            LoadingState()
        }
    }
}
