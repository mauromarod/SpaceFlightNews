package com.mauromarod.spaceflightnews.core.data.repository

import com.mauromarod.spaceflightnews.core.database.dao.ArticleDao
import com.mauromarod.spaceflightnews.core.database.dao.RemoteKeysDao
import com.mauromarod.spaceflightnews.core.database.entity.ArticleEntity
import com.mauromarod.spaceflightnews.core.domain.model.ArticleNotFoundException
import com.mauromarod.spaceflightnews.core.network.NetworkResult
import com.mauromarod.spaceflightnews.core.network.api.ArticleApi
import com.mauromarod.spaceflightnews.core.network.dto.ArticleDto
import com.mauromarod.spaceflightnews.core.network.dto.EventRefDto
import com.mauromarod.spaceflightnews.core.network.dto.LaunchRefDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.Instant
import java.util.concurrent.TimeUnit

class ArticleRepositoryImplTest {

    private val api: ArticleApi = mockk()
    private val articleDao: ArticleDao = mockk(relaxed = true)
    private val remoteKeysDao: RemoteKeysDao = mockk(relaxed = true)

    private lateinit var repository: ArticleRepositoryImpl

    @Before
    fun setUp() {
        repository = ArticleRepositoryImpl(api, articleDao, remoteKeysDao)
    }

    // --- observeArticles / observeSearchedArticles ---

    @Test
    fun `observeArticles returns a flow`() {
        val flow = repository.observeArticles()
        assertNotNull(flow)
    }

    @Test
    fun `observeSearchedArticles returns a flow`() {
        val flow = repository.observeSearchedArticles("SpaceX")
        assertNotNull(flow)
    }

    // --- getArticleDetail ---

    @Test
    fun `getArticleDetail returns cached article without network call`() = runTest {
        coEvery { articleDao.getById(1) } returns fakeEntity(1)

        val result = repository.getArticleDetail(1)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.id)
        coVerify(exactly = 0) { api.getArticle(any()) }
    }

    @Test
    fun `getArticleDetail fetches from network on cache miss and inserts to DB`() = runTest {
        coEvery { articleDao.getById(1) } returns null
        coEvery { api.getArticle(1) } returns NetworkResult.Success(fakeDto(1))

        val result = repository.getArticleDetail(1)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.id)
        coVerify(exactly = 1) { articleDao.insertAll(any()) }
    }

    @Test
    fun `getArticleDetail returns ArticleNotFoundException on 404`() = runTest {
        coEvery { articleDao.getById(99) } returns null
        coEvery { api.getArticle(99) } returns NetworkResult.HttpError(404, "Not Found")

        val result = repository.getArticleDetail(99)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ArticleNotFoundException)
    }

    @Test
    fun `getArticleDetail returns generic exception on non-404 HTTP error`() = runTest {
        coEvery { articleDao.getById(1) } returns null
        coEvery { api.getArticle(1) } returns NetworkResult.HttpError(500, "Server Error")

        val result = repository.getArticleDetail(1)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() !is ArticleNotFoundException)
        assertTrue(result.exceptionOrNull()?.message?.contains("500") == true)
    }

    @Test
    fun `getArticleDetail propagates NetworkError cause`() = runTest {
        val cause = IOException("timeout")
        coEvery { articleDao.getById(1) } returns null
        coEvery { api.getArticle(1) } returns NetworkResult.NetworkError(cause)

        val result = repository.getArticleDetail(1)

        assertTrue(result.isFailure)
        assertEquals(cause, result.exceptionOrNull())
    }

    @Test
    fun `getArticleDetail propagates UnknownError cause`() = runTest {
        val cause = RuntimeException("unexpected")
        coEvery { articleDao.getById(1) } returns null
        coEvery { api.getArticle(1) } returns NetworkResult.UnknownError(cause)

        val result = repository.getArticleDetail(1)

        assertTrue(result.isFailure)
        assertEquals(cause, result.exceptionOrNull())
    }

    // --- isDataStale ---

    @Test
    fun `isDataStale returns true when no lastFetchedAt exists`() {
        coEvery { remoteKeysDao.getLastFetchedAt() } returns null

        assertTrue(repository.isDataStale(ttlMinutes = 5))
    }

    @Test
    fun `isDataStale returns false when within TTL`() {
        val recentFetch = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2)
        coEvery { remoteKeysDao.getLastFetchedAt() } returns recentFetch

        assertTrue(!repository.isDataStale(ttlMinutes = 5))
    }

    @Test
    fun `isDataStale returns true when TTL has elapsed`() {
        val oldFetch = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10)
        coEvery { remoteKeysDao.getLastFetchedAt() } returns oldFetch

        assertTrue(repository.isDataStale(ttlMinutes = 5))
    }

    // --- getLastSyncedAt ---

    @Test
    fun `getLastSyncedAt returns null when no fetch timestamp exists`() = runTest {
        coEvery { remoteKeysDao.getLastFetchedAt() } returns null

        assertNull(repository.getLastSyncedAt())
    }

    @Test
    fun `getLastSyncedAt converts epoch millis to Instant`() = runTest {
        val epochMillis = Instant.parse("2025-05-11T10:00:00Z").toEpochMilli()
        coEvery { remoteKeysDao.getLastFetchedAt() } returns epochMillis

        val result = repository.getLastSyncedAt()

        assertEquals(Instant.ofEpochMilli(epochMillis), result)
    }

    // --- buildFtsQuery ---

    @Test
    fun `buildFtsQuery appends wildcard to single word`() {
        assertEquals("spacex*", ArticleRepositoryImpl.buildFtsQuery("spacex"))
    }

    @Test
    fun `buildFtsQuery appends wildcard to each word in multi-word query`() {
        assertEquals("space* x*", ArticleRepositoryImpl.buildFtsQuery("space x"))
    }

    @Test
    fun `buildFtsQuery trims leading and trailing whitespace before processing`() {
        assertEquals("spacex*", ArticleRepositoryImpl.buildFtsQuery("  spacex  "))
    }

    @Test
    fun `buildFtsQuery returns original empty string when query is blank`() {
        assertEquals("", ArticleRepositoryImpl.buildFtsQuery(""))
    }

    @Test
    fun `buildFtsQuery returns original whitespace-only string when all tokens empty`() {
        assertEquals("  ", ArticleRepositoryImpl.buildFtsQuery("  "))
    }

    // --- Helpers ---

    private fun fakeEntity(id: Int) = ArticleEntity(
        id = id,
        title = "Article $id",
        summary = "Summary",
        imageUrl = null,
        newsSite = "Space.com",
        publishedAt = Instant.parse("2024-01-15T12:00:00Z").toEpochMilli(),
        url = "https://example.com/$id",
        featured = false
    )

    private fun fakeDto(id: Int) = ArticleDto(
        id = id,
        title = "Article $id",
        url = "https://example.com/$id",
        imageUrl = null,
        newsSite = "Space.com",
        summary = "Summary",
        publishedAt = "2024-01-15T12:00:00Z",
        updatedAt = "2024-01-15T12:00:00Z",
        featured = false,
        launches = emptyList(),
        events = emptyList()
    )
}
