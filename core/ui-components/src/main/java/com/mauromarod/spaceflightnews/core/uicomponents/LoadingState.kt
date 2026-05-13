package com.mauromarod.spaceflightnews.core.uicomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mauromarod.spaceflightnews.core.designsystem.SpaceFlightNewsTheme
import com.mauromarod.spaceflightnews.core.designsystem.spacing

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    val loadingDesc = stringResource(R.string.cd_loading)
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = loadingDesc }
            .testTag(LoadingStateTags.CONTAINER),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        items(12) {
            ArticleCardSkeleton()
        }
    }
}

@Composable
private fun ArticleCardSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small)
    ) {
        ShimmerBox(
            modifier = Modifier
                .width(100.dp)
                .height(80.dp)
                .clip(MaterialTheme.shapes.medium)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = MaterialTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xSmall)
        ) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
            )
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
            )
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
            )
        }
    }
}

object LoadingStateTags {
    const val CONTAINER = "loading_state"
}

@Preview(name = "LoadingState — Light", showBackground = true)
@Composable
private fun LoadingStateLightPreview() {
    SpaceFlightNewsTheme(darkTheme = false) {
        Box(modifier = Modifier.height(400.dp)) {
            LoadingState()
        }
    }
}

@Preview(name = "LoadingState — Dark", showBackground = true)
@Composable
private fun LoadingStateDarkPreview() {
    SpaceFlightNewsTheme(darkTheme = true) {
        Box(modifier = Modifier.height(400.dp)) {
            LoadingState()
        }
    }
}
