package com.mauromarod.spaceflightnews.core.uicomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mauromarod.spaceflightnews.core.designsystem.SpaceFlightNewsTheme
import com.mauromarod.spaceflightnews.core.designsystem.VergeJellyMint
import com.mauromarod.spaceflightnews.core.designsystem.VergeSecondaryText
import com.mauromarod.spaceflightnews.core.designsystem.spacing

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.xLarge)
            .testTag(EmptyStateTags.CONTAINER),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = VergeJellyMint,
        )
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = MaterialTheme.spacing.medium)
                .testTag(EmptyStateTags.TITLE),
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = VergeSecondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = MaterialTheme.spacing.small)
                .testTag(EmptyStateTags.SUBTITLE),
        )
        if (action != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.spacing.large)
            ) {
                action()
            }
        }
    }
}

object EmptyStateTags {
    const val CONTAINER = "empty_state"
    const val TITLE = "empty_state_title"
    const val SUBTITLE = "empty_state_subtitle"
}

@Preview(name = "EmptyState", showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun EmptyStatePreview() {
    SpaceFlightNewsTheme {
        EmptyState(title = "No results found", subtitle = "Try searching for something else")
    }
}
