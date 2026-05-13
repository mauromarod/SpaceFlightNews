package com.mauromarod.spaceflightnews.core.uicomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mauromarod.spaceflightnews.core.designsystem.SpaceFlightNewsTheme
import com.mauromarod.spaceflightnews.core.designsystem.VergeCanvas
import com.mauromarod.spaceflightnews.core.designsystem.VergeHazardWhite
import com.mauromarod.spaceflightnews.core.designsystem.VergeJellyMint
import com.mauromarod.spaceflightnews.core.designsystem.VergeSecondaryText
import com.mauromarod.spaceflightnews.core.designsystem.spacing

@Composable
fun ArticleCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    image: @Composable () -> Unit,
    headline: @Composable () -> Unit,
    supporting: @Composable () -> Unit,
    badge: (@Composable () -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .clickable(onClick = onClick)
            .testTag(ArticleCardTags.CARD),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = VergeCanvas),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(width = 1.dp, color = VergeHazardWhite),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
            ) {
                image()
            }

            Column(
                modifier = Modifier.padding(MaterialTheme.spacing.medium)
            ) {
                badge?.invoke()
                headline()
                supporting()
            }
        }
    }
}

object ArticleCardTags {
    const val CARD = "article_card"
}

@Preview(name = "ArticleCard — StoryStream", showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun ArticleCardPreview() {
    SpaceFlightNewsTheme {
        ArticleCard(
            onClick = {},
            image = { ShimmerBox(Modifier.fillMaxWidth()) },
            headline = {
                Text(
                    text = "NASA reveals new findings from James Webb Space Telescope",
                    style = MaterialTheme.typography.headlineMedium,
                    color = VergeHazardWhite,
                    maxLines = 3,
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.xSmall),
                )
            },
            supporting = {
                Text(
                    text = "SPACE.COM · MAY 13",
                    style = MaterialTheme.typography.labelMedium,
                    color = VergeSecondaryText,
                )
            },
            badge = {
                Text(
                    text = "SCIENCE",
                    style = MaterialTheme.typography.labelMedium,
                    color = VergeJellyMint,
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.xSmall),
                )
            }
        )
    }
}
