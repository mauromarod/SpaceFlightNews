package com.mauromarod.spaceflightnews.core.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.mauromarod.spaceflightnews.core.database.entity.ArticleEntity
import com.mauromarod.spaceflightnews.core.network.NetworkResult
import com.mauromarod.spaceflightnews.core.network.api.ArticleApi
import com.mauromarod.spaceflightnews.core.database.dao.ArticleDao
import com.mauromarod.spaceflightnews.core.network.dto.ArticleDto
import com.mauromarod.spaceflightnews.core.network.dto.ArticleListResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class SearchRemoteMediatorTest {

    private val api: ArticleApi = mockk()
    private val articleDao: ArticleDao = mockk(relaxed = true)
    private val pagingConfig = PagingConfig(pageSize = ArticleRemoteMediator.PAGE_SIZE)

    private lateinit var mediator: SearchRemoteMediator

    @Before
    fun setUp() {
        mediator = SearchRemoteMediator("SpaceX", api, articleDao)
    }

    // --- PREPEND ---

    @Test
    fun `PREPEND always ends pagination without calling API`() = runTest {
        val result = mediator.load(LoadType.PREPEND, emptyState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        coVerify(exactly = 0) { api.searchArticles(any(), any(), any()) }
    }

    // --- REFRESH ---

    @Test
    fun `REFRESH success with more pages returns endOfPaginationReached false`() = runTest {
        coEvery { api.searchArticles("SpaceX", any(), 0) } returns
            NetworkResult.Success(fakeResponse(hasNextPage = true))

        val result = mediator.load(LoadType.REFRESH, emptyState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `REFRESH success with no more pages returns endOfPaginationReached true`() = runTest {
        coEvery { api.searchArticles("SpaceX", any(), 0) } returns
            NetworkResult.Success(fakeResponse(hasNextPage = false))

        val result = mediator.load(LoadType.REFRESH, emptyState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `REFRESH calls API with offset 0`() = runTest {
        coEvery { api.searchArticles("SpaceX", any(), 0) } returns
            NetworkResult.Success(fakeResponse(hasNextPage = true))

        mediator.load(LoadType.REFRESH, emptyState())

        coVerify(exactly = 1) { api.searchArticles("SpaceX", any(), 0) }
    }

    @Test
    fun `REFRESH inserts fetched articles into DAO`() = runTest {
        coEvery { api.searchArticles("SpaceX", any(), 0) } returns
            NetworkResult.Success(fakeResponse(hasNextPage = true))

        mediator.load(LoadType.REFRESH, emptyState())

        coVerify(exactly = 1) { articleDao.insertAll(any()) }
    }

    @Test
    fun `REFRESH HTTP error returns MediatorResult Error`() = runTest {
        coEvery { api.searchArticles("SpaceX", any(), 0) } returns
            NetworkResult.HttpError(500, "Server Error")

        val result = mediator.load(LoadType.REFRESH, emptyState())

        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }

    @Test
    fun `REFRESH network error returns MediatorResult Error with correct cause`() = runTest {
        val cause = IOException("No internet")
        coEvery { api.searchArticles("SpaceX", any(), 0) } returns
            NetworkResult.NetworkError(cause)

        val result = mediator.load(LoadType.REFRESH, emptyState())

        assertTrue(result is RemoteMediator.MediatorResult.Error)
        assertEquals(cause, (result as RemoteMediator.MediatorResult.Error).throwable)
    }

    @Test
    fun `REFRESH unknown error returns MediatorResult Error with correct cause`() = runTest {
        val cause = RuntimeException("unexpected")
        coEvery { api.searchArticles("SpaceX", any(), 0) } returns
            NetworkResult.UnknownError(cause)

        val result = mediator.load(LoadType.REFRESH, emptyState())

        assertTrue(result is RemoteMediator.MediatorResult.Error)
        assertEquals(cause, (result as RemoteMediator.MediatorResult.Error).throwable)
    }

    // --- APPEND ---

    @Test
    fun `APPEND after REFRESH uses accumulated nextOffset as API offset`() = runTest {
        val response = fakeResponse(hasNextPage = true, resultCount = 5)
        coEvery { api.searchArticles("SpaceX", any(), 0) } returns NetworkResult.Success(response)
        coEvery { api.searchArticles("SpaceX", any(), 5) } returns
            NetworkResult.Success(fakeResponse(hasNextPage = false))

        mediator.load(LoadType.REFRESH, emptyState())
        mediator.load(LoadType.APPEND, emptyState())

        coVerify(exactly = 1) { api.searchArticles("SpaceX", any(), 5) }
    }

    @Test
    fun `APPEND success accumulates nextOffset with each page loaded`() = runTest {
        val firstPage = fakeResponse(hasNextPage = true, resultCount = 5)
        val secondPage = fakeResponse(hasNextPage = true, resultCount = 3)
        coEvery { api.searchArticles("SpaceX", any(), 0) } returns NetworkResult.Success(firstPage)
        coEvery { api.searchArticles("SpaceX", any(), 5) } returns NetworkResult.Success(secondPage)
        coEvery { api.searchArticles("SpaceX", any(), 8) } returns
            NetworkResult.Success(fakeResponse(hasNextPage = false))

        mediator.load(LoadType.REFRESH, emptyState())
        mediator.load(LoadType.APPEND, emptyState())
        mediator.load(LoadType.APPEND, emptyState())

        coVerify(exactly = 1) { api.searchArticles("SpaceX", any(), 8) }
    }

    @Test
    fun `REFRESH resets nextOffset to 0 after prior APPEND`() = runTest {
        val firstPage = fakeResponse(hasNextPage = true, resultCount = 5)
        coEvery { api.searchArticles("SpaceX", any(), 0) } returns NetworkResult.Success(firstPage)
        coEvery { api.searchArticles("SpaceX", any(), 5) } returns
            NetworkResult.Success(fakeResponse(hasNextPage = false))

        mediator.load(LoadType.REFRESH, emptyState())
        mediator.load(LoadType.APPEND, emptyState())
        mediator.load(LoadType.REFRESH, emptyState())

        coVerify(exactly = 2) { api.searchArticles("SpaceX", any(), 0) }
    }

    @Test
    fun `APPEND network error returns MediatorResult Error`() = runTest {
        val cause = IOException("timeout")
        coEvery { api.searchArticles("SpaceX", any(), 0) } returns
            NetworkResult.Success(fakeResponse(hasNextPage = true, resultCount = 5))
        coEvery { api.searchArticles("SpaceX", any(), 5) } returns
            NetworkResult.NetworkError(cause)

        mediator.load(LoadType.REFRESH, emptyState())
        val result = mediator.load(LoadType.APPEND, emptyState())

        assertTrue(result is RemoteMediator.MediatorResult.Error)
        assertEquals(cause, (result as RemoteMediator.MediatorResult.Error).throwable)
    }

    // --- Helpers ---

    private fun emptyState() = PagingState<Int, ArticleEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = pagingConfig,
        leadingPlaceholderCount = 0
    )

    @Suppress("UnusedPrivateMember")
    private fun stateWithItems(items: List<ArticleEntity>): PagingState<Int, ArticleEntity> {
        val page = PagingSource.LoadResult.Page<Int, ArticleEntity>(
            data = items, prevKey = null, nextKey = null
        )
        return PagingState(
            pages = listOf(page),
            anchorPosition = items.lastIndex,
            config = pagingConfig,
            leadingPlaceholderCount = 0
        )
    }

    private fun fakeResponse(hasNextPage: Boolean, resultCount: Int = 2) = ArticleListResponseDto(
        count = 100,
        next = if (hasNextPage) "https://api.example.com/articles/?offset=20" else null,
        previous = null,
        results = (1..resultCount).map { fakeDto(it) }
    )

    private fun fakeDto(id: Int) = ArticleDto(
        id = id, title = "Article $id", url = "https://example.com/$id",
        imageUrl = null, newsSite = "Space.com", summary = "Summary",
        publishedAt = "2024-01-15T12:00:00Z",
        featured = false, launches = emptyList(), events = emptyList()
    )
}
