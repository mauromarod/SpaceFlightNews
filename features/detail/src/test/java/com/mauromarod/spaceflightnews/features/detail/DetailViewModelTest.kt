package com.mauromarod.spaceflightnews.features.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.core.domain.model.ArticleNotFoundException
import com.mauromarod.spaceflightnews.core.domain.repository.AnalyticsRepository
import com.mauromarod.spaceflightnews.core.domain.repository.ArticleRepository
import com.mauromarod.spaceflightnews.core.domain.repository.CrashReporter
import com.mauromarod.spaceflightnews.core.domain.usecase.GetArticleDetailUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ArticleRepository
    private lateinit var useCase: GetArticleDetailUseCase
    private val analyticsRepository: AnalyticsRepository = mockk(relaxed = true)
    private val crashReporter: CrashReporter = mockk(relaxed = true)

    private val fakeArticle = Article(
        id = 1,
        title = "SpaceX Launch",
        summary = "A successful launch.",
        imageUrl = "https://example.com/img.jpg",
        newsSite = "SpaceNews",
        publishedAt = Instant.now(),
        url = "https://example.com/article/1",
        featured = false
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        useCase = GetArticleDetailUseCase(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onInit loads article and emits Content state`() = runTest {
        coEvery { repository.getArticleDetail(1) } returns Result.success(fakeArticle)
        val viewModel = buildViewModel(articleId = 1)

        viewModel.uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(DetailUiState.Content(fakeArticle), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onInit article not found emits Error with isNotFound true`() = runTest {
        coEvery { repository.getArticleDetail(99) } returns
            Result.failure(ArticleNotFoundException(99))
        val viewModel = buildViewModel(articleId = 99)

        viewModel.uiState.test {
            awaitItem() // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            val error = awaitItem() as DetailUiState.Error
            assertTrue(error.isNotFound)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onInit network error emits Error with isNotFound false`() = runTest {
        coEvery { repository.getArticleDetail(1) } returns
            Result.failure(Exception("No internet"))
        val viewModel = buildViewModel(articleId = 1)

        viewModel.uiState.test {
            awaitItem() // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            val error = awaitItem() as DetailUiState.Error
            assertFalse(error.isNotFound)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OpenUrlClicked emits OpenUrl effect with article url`() = runTest {
        coEvery { repository.getArticleDetail(1) } returns Result.success(fakeArticle)
        val viewModel = buildViewModel(articleId = 1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(DetailUiEvent.OpenUrlClicked)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(DetailUiEffect.OpenUrl(fakeArticle.url), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `RetryClicked reloads article`() = runTest {
        coEvery { repository.getArticleDetail(1) } returns Result.success(fakeArticle)
        val viewModel = buildViewModel(articleId = 1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(DetailUiEvent.RetryClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 2) { repository.getArticleDetail(1) }
    }

    @Test
    fun `OpenUrlClicked when state is not Content does not emit effect`() = runTest {
        coEvery { repository.getArticleDetail(1) } returns
            Result.failure(Exception("error"))
        val viewModel = buildViewModel(articleId = 1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(DetailUiEvent.OpenUrlClicked)
            testDispatcher.scheduler.advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun buildViewModel(articleId: Int) = DetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf(DetailViewModel.ARTICLE_ID_KEY to articleId)),
        getArticleDetailUseCase = useCase,
        analyticsRepository = analyticsRepository,
        crashReporter = crashReporter,
    )
}
