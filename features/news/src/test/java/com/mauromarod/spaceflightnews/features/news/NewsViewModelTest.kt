package com.mauromarod.spaceflightnews.features.news

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import app.cash.turbine.test
import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.core.domain.repository.AnalyticsRepository
import com.mauromarod.spaceflightnews.core.domain.repository.ArticleRepository
import com.mauromarod.spaceflightnews.core.domain.usecase.GetArticlesUseCase
import com.mauromarod.spaceflightnews.core.domain.usecase.SearchArticlesUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeArticles = flowOf(PagingData.from(listOf(fakeArticle(1), fakeArticle(2))))

    private lateinit var repository: ArticleRepository
    private lateinit var getArticlesUseCase: GetArticlesUseCase
    private lateinit var searchArticlesUseCase: SearchArticlesUseCase
    private val analyticsRepository: AnalyticsRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        every { repository.getArticles() } returns fakeArticles
        every { repository.searchArticles(any()) } returns fakeArticles
        coEvery { repository.getLastSyncedAt() } returns null
        getArticlesUseCase = GetArticlesUseCase(repository)
        searchArticlesUseCase = SearchArticlesUseCase(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial search query is empty`() = runTest {
        val viewModel = buildViewModel()
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `search query is restored from SavedStateHandle`() = runTest {
        val viewModel = buildViewModel(savedQuery = "SpaceX")
        assertEquals("SpaceX", viewModel.searchQuery.value)
    }

    @Test
    fun `SearchQueryChanged updates searchQuery and saves to handle`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = buildViewModel(savedStateHandle = handle)
        viewModel.onEvent(NewsUiEvent.SearchQueryChanged("Mars"))
        assertEquals("Mars", viewModel.searchQuery.value)
        assertEquals("Mars", handle.get<String>("search_query"))
    }

    @Test
    fun `ArticleTapped emits NavigateToDetail effect`() = runTest {
        val viewModel = buildViewModel()
        viewModel.uiEffect.test {
            viewModel.onEvent(NewsUiEvent.ArticleTapped(42))
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(NewsUiEffect.NavigateToDetail(42), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `lastSyncedAt is null when repository returns null`() = runTest {
        coEvery { repository.getLastSyncedAt() } returns null
        val viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNull(viewModel.lastSyncedAt.value)
    }

    @Test
    fun `lastSyncedAt reflects repository value after init`() = runTest {
        val instant = Instant.parse("2025-05-11T10:00:00Z")
        coEvery { repository.getLastSyncedAt() } returns instant
        val viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(instant, viewModel.lastSyncedAt.value)
    }

    @Test
    fun `ArticleTapped with active search emits NavigateToDetail and tracks search converted`() = runTest {
        val viewModel = buildViewModel()
        viewModel.onEvent(NewsUiEvent.SearchQueryChanged("Mars"))
        viewModel.uiEffect.test {
            viewModel.onEvent(NewsUiEvent.ArticleTapped(7))
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(NewsUiEffect.NavigateToDetail(7), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        verify { analyticsRepository.trackSearchConverted(7) }
    }

    @Test
    fun `ArticleTapped with no search uses feed source`() = runTest {
        val viewModel = buildViewModel()
        viewModel.uiEffect.test {
            viewModel.onEvent(NewsUiEvent.ArticleTapped(3))
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(NewsUiEffect.NavigateToDetail(3), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onArticleImpression tracks article opened with feed source when no search`() = runTest {
        val viewModel = buildViewModel()
        viewModel.onArticleImpression(1, "Title", "NASA")
        verify { analyticsRepository.trackArticleOpened(1, "Title", "NASA", "feed") }
    }

    @Test
    fun `onArticleImpression tracks article opened with search source when query active`() = runTest {
        val viewModel = buildViewModel()
        viewModel.onEvent(NewsUiEvent.SearchQueryChanged("Apollo"))
        viewModel.onArticleImpression(2, "Apollo 11", "Space.com")
        verify { analyticsRepository.trackArticleOpened(2, "Apollo 11", "Space.com", "search") }
    }

    @Test
    fun `RetryClicked does not emit any effect`() = runTest {
        val viewModel = buildViewModel()
        viewModel.uiEffect.test {
            viewModel.onEvent(NewsUiEvent.RetryClicked)
            testDispatcher.scheduler.advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun buildViewModel(
        savedQuery: String? = null,
        savedStateHandle: SavedStateHandle = SavedStateHandle(
            if (savedQuery != null) mapOf("search_query" to savedQuery) else emptyMap()
        )
    ) = NewsViewModel(
        savedStateHandle = savedStateHandle,
        getArticlesUseCase = getArticlesUseCase,
        searchArticlesUseCase = searchArticlesUseCase,
        repository = repository,
        analyticsRepository = analyticsRepository,
    )

    private fun fakeArticle(id: Int) = Article(
        id = id,
        title = "Article $id",
        summary = "Summary $id",
        imageUrl = null,
        newsSite = "Space.com",
        publishedAt = Instant.now(),
        url = "https://example.com/$id",
        featured = false
    )
}
