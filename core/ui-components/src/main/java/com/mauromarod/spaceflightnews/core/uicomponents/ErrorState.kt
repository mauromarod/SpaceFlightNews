package com.mauromarod.spaceflightnews.core.uicomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.mauromarod.spaceflightnews.core.designsystem.VergeConsoleMintBorder
import com.mauromarod.spaceflightnews.core.designsystem.VergeJellyMint
import com.mauromarod.spaceflightnews.core.designsystem.VergeSecondaryText
import com.mauromarod.spaceflightnews.core.designsystem.VergeUltraviolet
import com.mauromarod.spaceflightnews.core.designsystem.spacing

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.xLarge)
            .testTag(ErrorStateTags.CONTAINER),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = VergeUltraviolet,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = MaterialTheme.spacing.medium)
                .testTag(ErrorStateTags.MESSAGE),
        )
        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.spacing.large)
                .testTag(ErrorStateTags.RETRY_BUTTON),
            shape = MaterialTheme.shapes.extraLarge,
            border = BorderStroke(1.dp, VergeConsoleMintBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VergeJellyMint),
        ) {
            Text(
                text = stringResource(R.string.action_retry).uppercase(),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

object ErrorStateTags {
    const val CONTAINER = "error_state"
    const val MESSAGE = "error_state_message"
    const val RETRY_BUTTON = "error_state_retry"
}

@Preview(name = "ErrorState", showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun ErrorStatePreview() {
    SpaceFlightNewsTheme {
        ErrorState(message = "No internet connection", onRetry = {})
    }
}
