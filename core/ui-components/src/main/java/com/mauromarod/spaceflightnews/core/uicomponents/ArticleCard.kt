package com.mauromarod.spaceflightnews.core.uicomponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mauromarod.spaceflightnews.core.designsystem.SpaceFlightNewsTheme
import com.mauromarod.spaceflightnews.core.designsystem.spacing

@Composable
fun ArticleCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    image: @Composable () -> Unit,
    headline: @Composable () -> Unit,
    supporting: @Composable () -> Unit,
    badge: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .clickable(onClick = onClick)
            .testTag(ArticleCardTags.CARD),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.small),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(80.dp)
            ) {
                image()
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = MaterialTheme.spacing.small)
            ) {
                headline()
                supporting()
                badge?.invoke()
            }
        }
    }
}

object ArticleCardTags {
    const val CARD = "article_card"
}

@Preview(name = "ArticleCard — Light", showBackground = true)
@Composable
private fun ArticleCardLightPreview() {
    SpaceFlightNewsTheme(darkTheme = false) {
        ArticleCard(
            onClick = {},
            image = {
                ShimmerBox(Modifier.fillMaxWidth())
            },
            headline = {
                Text(
                    text = "NASA reveals new findings from James Webb Space Telescope",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2
                )
            },
            supporting = {
                Text(
                    text = "Space.com · 2h ago",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    }
}

@Preview(name = "ArticleCard — Dark", showBackground = true)
@Composable
private fun ArticleCardDarkPreview() {
    SpaceFlightNewsTheme(darkTheme = true) {
        ArticleCard(
            onClick = {},
            image = {
                ShimmerBox(Modifier.fillMaxWidth())
            },
            headline = {
                Text(
                    text = "SpaceX Starship completes first successful orbital test",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2
                )
            },
            supporting = {
                Text(
                    text = "SpaceNews · 5h ago",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    }
}
