package com.mauromarod.spaceflightnews.core.uicomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mauromarod.spaceflightnews.core.designsystem.SpaceFlightNewsTheme
import com.mauromarod.spaceflightnews.core.designsystem.spacing

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.xLarge)
            .testTag(ErrorStateTags.CONTAINER),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag(ErrorStateTags.MESSAGE)
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        Button(
            onClick = onRetry,
            modifier = Modifier.testTag(ErrorStateTags.RETRY_BUTTON)
        ) {
            Text(text = stringResource(R.string.action_retry))
        }
    }
}

object ErrorStateTags {
    const val CONTAINER = "error_state"
    const val MESSAGE = "error_state_message"
    const val RETRY_BUTTON = "error_state_retry"
}

@Preview(name = "ErrorState — Light", showBackground = true)
@Composable
private fun ErrorStateLightPreview() {
    SpaceFlightNewsTheme(darkTheme = false) {
        ErrorState(
            message = "No internet connection. Check your connection and try again.",
            onRetry = {}
        )
    }
}

@Preview(name = "ErrorState — Dark", showBackground = true)
@Composable
private fun ErrorStateDarkPreview() {
    SpaceFlightNewsTheme(darkTheme = true) {
        ErrorState(
            message = "Something went wrong. Please try again.",
            onRetry = {}
        )
    }
}
