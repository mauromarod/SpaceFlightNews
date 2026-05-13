package com.mauromarod.spaceflightnews

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mauromarod.spaceflightnews.core.designsystem.SpaceFlightNewsTheme
import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.features.detail.DetailScreen
import com.mauromarod.spaceflightnews.features.detail.DetailUiEffect
import com.mauromarod.spaceflightnews.features.detail.DetailUiState
import com.mauromarod.spaceflightnews.features.detail.DetailViewModel
import com.mauromarod.spaceflightnews.robot.ArticleDetailRobot
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class DetailScreenTest {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val fakeArticle = Article(
        id = 1,
        title = "SpaceX Starship Test Flight",
        summary = "A landmark achievement in reusable rocket technology.",
        imageUrl = "https://example.com/img.jpg",
        newsSite = "SpaceNews",
        publishedAt = Instant.parse("2024-03-14T18:00:00Z"),
        url = "https://example.com/article/1",
        featured = true
    )

    private fun buildViewModel(state: DetailUiState): DetailViewModel =
        mockk(relaxed = true) {
            every { uiState } returns MutableStateFlow(state)
            every { uiEffect } returns emptyFlow<DetailUiEffect>()
        }

    @Test
    fun contentState_showsArticleTitleAndReadButton() {
        composeTestRule.setContent {
            SpaceFlightNewsTheme {
                DetailScreen(
                    viewModel = buildViewModel(DetailUiState.Content(fakeArticle)),
                    onBack = {}
                )
            }
        }

        ArticleDetailRobot(composeTestRule).check {
            assertTitleVisible(fakeArticle.title)
            assertReadButtonVisible()
        }
    }

    @Test
    fun errorState_showsErrorContainer() {
        composeTestRule.setContent {
            SpaceFlightNewsTheme {
                DetailScreen(
                    viewModel = buildViewModel(DetailUiState.Error(message = "Not found", isNotFound = true)),
                    onBack = {}
                )
            }
        }

        ArticleDetailRobot(composeTestRule).check { assertErrorStateVisible() }
    }
}
