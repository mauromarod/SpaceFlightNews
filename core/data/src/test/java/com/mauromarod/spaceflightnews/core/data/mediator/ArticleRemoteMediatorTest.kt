package com.mauromarod.spaceflightnews.core.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.mauromarod.spaceflightnews.core.database.AppDatabase
import com.mauromarod.spaceflightnews.core.database.dao.ArticleDao
import com.mauromarod.spaceflightnews.core.database.dao.RemoteKeysDao
import com.mauromarod.spaceflightnews.core.database.entity.ArticleEntity
import com.mauromarod.spaceflightnews.core.database.entity.RemoteKeysEntity
import com.mauromarod.spaceflightnews.core.network.NetworkResult
import com.mauromarod.spaceflightnews.core.network.api.ArticleApi
import com.mauromarod.spaceflightnews.core.network.dto.ArticleDto
import com.mauromarod.spaceflightnews.core.network.dto.ArticleListResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import io.mockk.Ordering
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class ArticleRemoteMediatorTest {

    private val api: ArticleApi = mockk()
    private val database: AppDatabase = mockk(relaxed = true)
    private val articleDao: ArticleDao = mockk(relaxed = true)
    private val remoteKeysDao: RemoteKeysDao = mockk(relaxed = true)
    private val pagingConfig = PagingConfig(pageSize = ArticleRemoteMediator.PAGE_SIZE)

    private lateinit var mediator: ArticleRemoteMediator

    @Before
    fun setUp() {
        mockkStatic("androidx.room.RoomDatabaseKt")
        // withTransaction<Unit> to help the compiler resolve the generic R = Unit
        coEvery { database.withTransaction<Unit>(any()) } just runs
        mediator = ArticleRemoteMediator(api, database, articleDao, remoteKeysDao)
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    // --- PREPEND ---

    @Test
    fun `PREPEND always ends pagination`() = runTest {
        val result = mediator.load(LoadType.PREPEND, emptyState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    // --- REFRESH ---

    @Test
    fun `REFRESH success with more pages returns endOfPaginationReached false`() = runTest {
        coEvery { api.getArticles(any(), 0) } returns NetworkResult.Success(fakeResponse(true))

        val result = mediator.load(LoadType.REFRESH, emptyState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `REFRESH success with no more pages returns endOfPaginationReached true`() = runTest {
        coEvery { api.getArticles(any(), 0) } returns NetworkResult.Success(fakeResponse(false))

        val result = mediator.load(LoadType.REFRESH, emptyState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `REFRESH success uses a database transaction`() = runTest {
        coEvery { api.getArticles(any(), 0) } returns NetworkResult.Success(fakeResponse(true))

        mediator.load(LoadType.REFRESH, emptyState())

        coVerify(exactly = 1) { database.withTransaction<Unit>(any()) }
    }

    @Test
    fun `REFRESH network error returns MediatorResult Error`() = runTest {
        val cause = IOException("No internet")
        coEvery { api.getArticles(any(), 0) } returns NetworkResult.NetworkError(cause)

        val result = mediator.load(LoadType.REFRESH, emptyState())

        assertTrue(result is RemoteMediator.MediatorResult.Error)
        assertEquals(cause, (result as RemoteMediator.MediatorResult.Error).throwable)
    }

    @Test
    fun `REFRESH HTTP error returns MediatorResult Error`() = runTest {
        coEvery { api.getArticles(any(), 0) } returns NetworkResult.HttpError(500, "Server Error")

        val result = mediator.load(LoadType.REFRESH, emptyState())

        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }

    // --- APPEND ---

    @Test
    fun `APPEND with empty pages returns endOfPaginationReached false`() = runTest {
        // Regression test: was incorrectly returning true, permanently blocking future loads
        val result = mediator.load(LoadType.APPEND, emptyState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        coVerify(exactly = 0) { api.getArticles(any(), any()) }
    }

    @Test
    fun `APPEND with missing remoteKey returns endOfPaginationReached false`() = runTest {
        // Regression test: was incorrectly returning true
        coEvery { remoteKeysDao.getByArticleId(any()) } returns null

        val result = mediator.load(LoadType.APPEND, stateWithItems(listOf(fakeEntity(1))))

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        coVerify(exactly = 0) { api.getArticles(any(), any()) }
    }

    @Test
    fun `APPEND with null nextKey returns endOfPaginationReached true`() = runTest {
        coEvery { remoteKeysDao.getByArticleId(1) } returns
            RemoteKeysEntity(articleId = 1, prevKey = 0, nextKey = null)

        val result = mediator.load(LoadType.APPEND, stateWithItems(listOf(fakeEntity(1))))

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        coVerify(exactly = 0) { api.getArticles(any(), any()) }
    }

    @Test
    fun `APPEND with valid nextKey calls API with correct offset`() = runTest {
        coEvery { remoteKeysDao.getByArticleId(1) } returns
            RemoteKeysEntity(articleId = 1, prevKey = 0, nextKey = 32)
        coEvery { api.getArticles(any(), 32) } returns NetworkResult.Success(fakeResponse(true))

        val result = mediator.load(LoadType.APPEND, stateWithItems(listOf(fakeEntity(1))))

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        coVerify(exactly = 1) { api.getArticles(any(), 32) }
    }

    @Test
    fun `APPEND network error returns MediatorResult Error`() = runTest {
        val cause = IOException("timeout")
        coEvery { remoteKeysDao.getByArticleId(1) } returns
            RemoteKeysEntity(articleId = 1, prevKey = 0, nextKey = 32)
        coEvery { api.getArticles(any(), 32) } returns NetworkResult.NetworkError(cause)

        val result = mediator.load(LoadType.APPEND, stateWithItems(listOf(fakeEntity(1))))

        assertTrue(result is RemoteMediator.MediatorResult.Error)
        assertEquals(cause, (result as RemoteMediator.MediatorResult.Error).throwable)
    }

    // --- persist() ---
    // Tested directly to cover the transaction body without fighting withTransaction mocking.
    // The transaction wrapper itself is verified by the REFRESH tests above.

    @Test
    fun `persist REFRESH clears both tables before inserting`() = runTest {
        val articles = listOf(fakeEntity(1), fakeEntity(2))

        mediator.persist(LoadType.REFRESH, articles, prevKey = null, nextKey = 32, now = 1000L)

        coVerify(ordering = Ordering.ORDERED) {
            remoteKeysDao.clearAll()
            articleDao.clearAll()
            articleDao.insertAll(any())
        }
    }

    @Test
    fun `persist APPEND does not clear tables`() = runTest {
        val articles = listOf(fakeEntity(1))

        mediator.persist(LoadType.APPEND, articles, prevKey = 0, nextKey = 64, now = 1000L)

        coVerify(exactly = 0) { remoteKeysDao.clearAll() }
        coVerify(exactly = 0) { articleDao.clearAll() }
        coVerify(exactly = 1) { articleDao.insertAll(any()) }
    }

    @Test
    fun `persist REFRESH stores lastFetchedAt in remote keys`() = runTest {
        val articles = listOf(fakeEntity(1))
        val now = 9_999_000L
        val slot = io.mockk.slot<List<com.mauromarod.spaceflightnews.core.database.entity.RemoteKeysEntity>>()
        coEvery { remoteKeysDao.insertAll(capture(slot)) } returns Unit

        mediator.persist(LoadType.REFRESH, articles, prevKey = null, nextKey = 32, now = now)

        assertEquals(now, slot.captured.first().lastFetchedAt)
    }

    @Test
    fun `persist APPEND stores null lastFetchedAt in remote keys`() = runTest {
        val articles = listOf(fakeEntity(1))
        val slot = io.mockk.slot<List<com.mauromarod.spaceflightnews.core.database.entity.RemoteKeysEntity>>()
        coEvery { remoteKeysDao.insertAll(capture(slot)) } returns Unit

        mediator.persist(LoadType.APPEND, articles, prevKey = 0, nextKey = 64, now = 1000L)

        assertNull(slot.captured.first().lastFetchedAt)
    }

    @Test
    fun `persist stores correct nextKey and prevKey in remote keys`() = runTest {
        val articles = listOf(fakeEntity(1))
        val slot = io.mockk.slot<List<com.mauromarod.spaceflightnews.core.database.entity.RemoteKeysEntity>>()
        coEvery { remoteKeysDao.insertAll(capture(slot)) } returns Unit

        mediator.persist(LoadType.REFRESH, articles, prevKey = null, nextKey = 32, now = 0L)

        val key = slot.captured.first()
        assertNull(key.prevKey)
        assertEquals(32, key.nextKey)
    }

    // --- Helpers ---

    private fun emptyState() = PagingState<Int, ArticleEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = pagingConfig,
        leadingPlaceholderCount = 0
    )

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

    private fun fakeResponse(hasNextPage: Boolean) = ArticleListResponseDto(
        count = 100,
        next = if (hasNextPage) "https://api.example.com/articles/?offset=32" else null,
        previous = null,
        results = listOf(fakeDto(1), fakeDto(2))
    )

    private fun fakeEntity(id: Int) = ArticleEntity(
        id = id, title = "Article $id", summary = "Summary",
        imageUrl = null, newsSite = "Space.com", publishedAt = 1_000_000L,
        url = "https://example.com/$id", featured = false
    )

    private fun fakeDto(id: Int) = ArticleDto(
        id = id, title = "Article $id", url = "https://example.com/$id",
        imageUrl = null, newsSite = "Space.com", summary = "Summary",
        publishedAt = "2024-01-15T12:00:00Z", updatedAt = "2024-01-15T12:00:00Z",
        featured = false, launches = emptyList(), events = emptyList()
    )
}
