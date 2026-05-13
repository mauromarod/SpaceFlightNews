package com.mauromarod.spaceflightnews.core.uicomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.mauromarod.spaceflightnews.core.designsystem.spacing

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.xLarge)
            .testTag(EmptyStateTags.CONTAINER),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag(EmptyStateTags.TITLE)
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag(EmptyStateTags.SUBTITLE)
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            action()
        }
    }
}

object EmptyStateTags {
    const val CONTAINER = "empty_state"
    const val TITLE = "empty_state_title"
    const val SUBTITLE = "empty_state_subtitle"
}

@Preview(name = "EmptyState — Light", showBackground = true)
@Composable
private fun EmptyStateLightPreview() {
    SpaceFlightNewsTheme(darkTheme = false) {
        EmptyState(
            title = "No results found",
            subtitle = "Try searching for something else"
        )
    }
}

@Preview(name = "EmptyState — Dark", showBackground = true)
@Composable
private fun EmptyStateDarkPreview() {
    SpaceFlightNewsTheme(darkTheme = true) {
        EmptyState(
            title = "No results found",
            subtitle = "Try searching for something else"
        )
    }
}
