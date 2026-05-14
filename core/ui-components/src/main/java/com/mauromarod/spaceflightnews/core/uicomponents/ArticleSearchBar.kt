package com.mauromarod.spaceflightnews.core.uicomponents

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mauromarod.spaceflightnews.core.designsystem.SpaceFlightNewsTheme
import com.mauromarod.spaceflightnews.core.designsystem.VergeCanvas
import com.mauromarod.spaceflightnews.core.designsystem.VergeHazardWhite
import com.mauromarod.spaceflightnews.core.designsystem.VergeJellyMint
import com.mauromarod.spaceflightnews.core.designsystem.VergeSecondaryText

@Composable
fun ArticleSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClearQuery: () -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .testTag(ArticleSearchBarTags.FIELD),
        placeholder = {
            Text(
                text = placeholder,
                color = VergeSecondaryText,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = VergeSecondaryText,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = onClearQuery,
                    modifier = Modifier.testTag(ArticleSearchBarTags.CLEAR_BUTTON),
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.cd_clear_search),
                        tint = VergeSecondaryText,
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(2.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = VergeCanvas,
            unfocusedContainerColor = VergeCanvas,
            focusedBorderColor = VergeJellyMint,
            unfocusedBorderColor = VergeHazardWhite,
            focusedTextColor = VergeHazardWhite,
            unfocusedTextColor = VergeHazardWhite,
            cursorColor = VergeJellyMint,
            focusedLeadingIconColor = VergeJellyMint,
            unfocusedLeadingIconColor = VergeSecondaryText,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
    )
}

object ArticleSearchBarTags {
    const val FIELD = "search_bar_field"
    const val CLEAR_BUTTON = "search_bar_clear"
}

@Preview(name = "SearchBar — Empty", showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun SearchBarEmptyPreview() {
    SpaceFlightNewsTheme {
        ArticleSearchBar(query = "", onQueryChange = {}, onSearch = {}, onClearQuery = {}, placeholder = "Search articles...")
    }
}

@Preview(name = "SearchBar — With Query", showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun SearchBarWithQueryPreview() {
    SpaceFlightNewsTheme {
        ArticleSearchBar(query = "SpaceX", onQueryChange = {}, onSearch = {}, onClearQuery = {}, placeholder = "Search articles...")
    }
}
