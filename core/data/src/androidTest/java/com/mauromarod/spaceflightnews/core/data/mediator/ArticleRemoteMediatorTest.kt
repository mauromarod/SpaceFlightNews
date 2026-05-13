package com.mauromarod.spaceflightnews.core.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mauromarod.spaceflightnews.core.database.AppDatabase
import com.mauromarod.spaceflightnews.core.database.entity.ArticleEntity
import com.mauromarod.spaceflightnews.core.network.adapter.NetworkResultCallAdapterFactory
import com.mauromarod.spaceflightnews.core.network.api.ArticleApi
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@OptIn(ExperimentalPagingApi::class)
@RunWith(AndroidJUnit4::class)
class ArticleRemoteMediatorTest {

    private val mockWebServer = MockWebServer()
    private lateinit var database: AppDatabase
    private lateinit var api: ArticleApi

    @Before
    fun setUp() {
        mockWebServer.start()
        val moshi = Moshi.Builder().build()
        api = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(NetworkResultCallAdapterFactory.create())
            .build()
            .create(ArticleApi::class.java)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        if (::database.isInitialized) database.close()
    }

    @Test
    fun refresh_insertsFirstPageAndPersistsRemoteKeys() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody(articlesPageJson(ids = listOf(1, 2), hasNext = true)))

        val mediator = buildMediator()
        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertTrue(result is androidx.paging.RemoteMediator.MediatorResult.Success)
        assertFalse((result as androidx.paging.RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertNotNull(database.articleDao().getById(1))
        assertNotNull(database.articleDao().getById(2))
        assertNotNull(database.remoteKeysDao().getByArticleId(1))
    }

    @Test
    fun refresh_endOfPagination_detectedWhenNextIsNull() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody(articlesPageJson(ids = listOf(1), hasNext = false)))

        val mediator = buildMediator()
        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertTrue(result is androidx.paging.RemoteMediator.MediatorResult.Success)
        assertTrue((result as androidx.paging.RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun refresh_clearsExistingDataBeforeInserting() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody(articlesPageJson(ids = listOf(10), hasNext = false)))
        buildMediator().load(LoadType.REFRESH, emptyPagingState())

        mockWebServer.enqueue(MockResponse().setBody(articlesPageJson(ids = listOf(20), hasNext = false)))
        buildMediator().load(LoadType.REFRESH, emptyPagingState())

        assertNull(database.articleDao().getById(10))
        assertNotNull(database.articleDao().getById(20))
    }

    @Test
    fun networkError_returnsMediatorError() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        val mediator = buildMediator()
        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertTrue(result is androidx.paging.RemoteMediator.MediatorResult.Error)
    }

    private fun buildMediator() = ArticleRemoteMediator(
        api = api,
        database = database,
        articleDao = database.articleDao(),
        remoteKeysDao = database.remoteKeysDao()
    )

    private fun emptyPagingState() = PagingState<Int, ArticleEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = ArticleRemoteMediator.PAGE_SIZE),
        leadingPlaceholderCount = 0
    )

    private fun articlesPageJson(ids: List<Int>, hasNext: Boolean): String {
        val nextValue = if (hasNext) "\"https://api.spaceflightnewsapi.net/v4/articles/?offset=20\"" else "null"
        val articles = ids.joinToString(",") { id ->
            """{"id":$id,"title":"Article $id","url":"https://example.com/$id","image_url":null,"news_site":"SpaceNews","summary":"Summary $id","published_at":"2024-01-01T00:00:00Z","updated_at":"2024-01-01T00:00:00Z","featured":false,"launches":[],"events":[]}"""
        }
        return """{"count":${ids.size + if (hasNext) 20 else 0},"next":$nextValue,"previous":null,"results":[$articles]}"""
    }
}
