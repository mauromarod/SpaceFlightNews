package com.mauromarod.spaceflightnews

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mauromarod.spaceflightnews.core.designsystem.SpaceFlightNewsTheme
import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.features.news.NewsUiEvent
import com.mauromarod.spaceflightnews.features.news.NewsScreen
import com.mauromarod.spaceflightnews.features.news.NewsViewModel
import com.mauromarod.spaceflightnews.robot.ArticleListRobot
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class NewsScreenTest {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun fakeArticle(id: Int = 1) = Article(
        id = id, title = "Article $id", summary = "Summary $id",
        imageUrl = null, newsSite = "SpaceNews",
        publishedAt = Instant.now(), url = "https://example.com/$id", featured = false
    )

    private fun buildViewModel(
        articles: List<Article> = listOf(fakeArticle(1), fakeArticle(2)),
        loadStates: LoadStates = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = false),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = false)
        )
    ): NewsViewModel = mockk(relaxed = true) {
        every { searchQuery } returns MutableStateFlow("")
        every { this@mockk.articles } returns flowOf(PagingData.from(articles, loadStates))
        every { onEvent(any<NewsUiEvent>()) } returns Unit
    }

    @Test
    fun launch_showsArticleList() {
        composeTestRule.setContent {
            SpaceFlightNewsTheme {
                NewsScreen(viewModel = buildViewModel(), onNavigateToDetail = {}, onNavigateToProfile = {})
            }
        }

        ArticleListRobot(composeTestRule).check { assertListVisible() }
    }

    @Test
    fun articlesAreDisplayed() {
        composeTestRule.setContent {
            SpaceFlightNewsTheme {
                NewsScreen(
                    viewModel = buildViewModel(articles = listOf(fakeArticle(1), fakeArticle(2))),
                    onNavigateToDetail = {},
                    onNavigateToProfile = {}
                )
            }
        }

        ArticleListRobot(composeTestRule).check { assertArticleCount(2) }
    }

    @Test
    fun emptyResultsWithNotLoading_showsEmptyState() {
        val emptyViewModel = mockk<NewsViewModel>(relaxed = true) {
            every { searchQuery } returns MutableStateFlow("NoResults")
            every { this@mockk.articles } returns flowOf(
                PagingData.empty(
                    LoadStates(
                        refresh = LoadState.NotLoading(endOfPaginationReached = true),
                        prepend = LoadState.NotLoading(endOfPaginationReached = true),
                        append = LoadState.NotLoading(endOfPaginationReached = true)
                    )
                )
            )
        }

        composeTestRule.setContent {
            SpaceFlightNewsTheme {
                NewsScreen(viewModel = emptyViewModel, onNavigateToDetail = {}, onNavigateToProfile = {})
            }
        }

        ArticleListRobot(composeTestRule).check { assertEmptyStateVisible() }
    }

    @Test
    fun errorState_showsRetryButton() {
        val errorViewModel = mockk<NewsViewModel>(relaxed = true) {
            every { searchQuery } returns MutableStateFlow("")
            every { this@mockk.articles } returns flowOf(
                PagingData.empty(
                    LoadStates(
                        refresh = LoadState.Error(Exception("Network error")),
                        prepend = LoadState.NotLoading(endOfPaginationReached = true),
                        append = LoadState.NotLoading(endOfPaginationReached = true)
                    )
                )
            )
        }

        composeTestRule.setContent {
            SpaceFlightNewsTheme {
                NewsScreen(viewModel = errorViewModel, onNavigateToDetail = {}, onNavigateToProfile = {})
            }
        }

        ArticleListRobot(composeTestRule).check { assertRetryButtonVisible() }
    }

    @Test
    fun tappingArticleCard_callsOnNavigateToDetail() {
        var tappedId: Int? = null
        composeTestRule.setContent {
            SpaceFlightNewsTheme {
                NewsScreen(
                    viewModel = buildViewModel(),
                    onNavigateToDetail = { tappedId = it },
                    onNavigateToProfile = {}
                )
            }
        }

        ArticleListRobot(composeTestRule).perform { tapFirstArticle() }

        assertEquals(1, tappedId)
    }

    @Test
    fun tappingProfileButton_callsOnNavigateToProfile() {
        var profileCalled = false
        composeTestRule.setContent {
            SpaceFlightNewsTheme {
                NewsScreen(
                    viewModel = buildViewModel(),
                    onNavigateToDetail = {},
                    onNavigateToProfile = { profileCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Open profile").performClick()

        assertTrue(profileCalled)
    }
}
