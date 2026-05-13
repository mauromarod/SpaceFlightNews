package com.mauromarod.spaceflightnews

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mauromarod.spaceflightnews.core.designsystem.SpaceFlightNewsTheme
import com.mauromarod.spaceflightnews.navigation.TwoPaneContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Validates the two-pane layout structure without nested NavHost.
 */
@RunWith(AndroidJUnit4::class)
class TwoPaneNavigationTest {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun twoPaneLayout_showsEmptyPane_whenNoArticleSelected() {
        composeTestRule.setContent {
            SpaceFlightNewsTheme {
                TwoPaneTestHost(selectedArticleId = null)
            }
        }

        composeTestRule.onNodeWithTag("detail_empty_pane").assertIsDisplayed()
    }

    @Test
    fun twoPaneLayout_showsArticleList() {
        composeTestRule.setContent {
            SpaceFlightNewsTheme {
                TwoPaneTestHost(selectedArticleId = null)
            }
        }

        composeTestRule.onNodeWithTag("news_article_list").assertIsDisplayed()
    }
}

@Composable
private fun TwoPaneTestHost(selectedArticleId: Int?) {
    val outerNav = rememberNavController()
    var id by remember { mutableStateOf(selectedArticleId) }

    NavHost(
        navController = outerNav,
        startDestination = "news",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("news") {
            TwoPaneContent(
                selectedArticleId = id,
                onArticleSelected = { id = it },
                onArticleDeselected = { id = null },
                onNavigateToProfile = { /* no-op */ },
            )
        }
    }
}
