package com.mauromarod.spaceflightnews.core.domain.usecase

import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.core.domain.model.ArticleNotFoundException
import com.mauromarod.spaceflightnews.core.domain.repository.ArticleRepository
import com.mauromarod.spaceflightnews.core.domain.repository.RemoteConfigRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class GetArticleDetailUseCaseTest {
    private val testDispatcher = StandardTestDispatcher()
    private val repository: ArticleRepository = mockk()
    private val remoteConfig: RemoteConfigRepository = mockk()
    private val useCase = GetArticleDetailUseCase(repository, remoteConfig)

    private val fakeArticle =
        Article(
            id = 1,
            title = "SpaceX Launch",
            summary = "A successful launch.",
            imageUrl = null,
            newsSite = "SpaceNews",
            publishedAt = Instant.parse("2024-01-15T12:00:00Z"),
            url = "https://example.com/1",
            featured = false,
        )

    @Before fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { remoteConfig.getCacheTtlMinutes() } returns 5
        every { repository.isDataStale(any()) } returns false
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke success propagates article`() =
        runTest {
            coEvery { repository.getArticleDetail(1) } returns Result.success(fakeArticle)

            val result = useCase(1)

            assertTrue(result.isSuccess)
            assertEquals(fakeArticle, result.getOrNull())
            coVerify(exactly = 1) { repository.getArticleDetail(1) }
        }

    @Test
    fun `invoke failure propagates exception`() =
        runTest {
            val exception = Exception("Network error")
            coEvery { repository.getArticleDetail(1) } returns Result.failure(exception)

            val result = useCase(1)

            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
        }

    @Test
    fun `invoke not found propagates ArticleNotFoundException`() =
        runTest {
            coEvery { repository.getArticleDetail(99) } returns Result.failure(ArticleNotFoundException(99))

            val result = useCase(99)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is ArticleNotFoundException)
        }
}
