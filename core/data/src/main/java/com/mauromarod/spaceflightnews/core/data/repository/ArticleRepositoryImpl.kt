package com.mauromarod.spaceflightnews.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.mauromarod.spaceflightnews.core.data.mapper.toDomain
import com.mauromarod.spaceflightnews.core.data.mapper.toEntity
import com.mauromarod.spaceflightnews.core.data.mediator.ArticleRemoteMediator
import com.mauromarod.spaceflightnews.core.data.mediator.SearchRemoteMediator
import com.mauromarod.spaceflightnews.core.database.AppDatabase
import com.mauromarod.spaceflightnews.core.database.dao.ArticleDao
import com.mauromarod.spaceflightnews.core.database.dao.RemoteKeysDao
import com.mauromarod.spaceflightnews.core.database.mapper.toDomain
import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.core.domain.model.ArticleNotFoundException
import com.mauromarod.spaceflightnews.core.domain.repository.AnalyticsRepository
import com.mauromarod.spaceflightnews.core.domain.repository.ArticleRepository
import com.mauromarod.spaceflightnews.core.domain.repository.PerformanceTracer
import com.mauromarod.spaceflightnews.core.network.NetworkResult
import com.mauromarod.spaceflightnews.core.network.api.ArticleApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.concurrent.TimeUnit

private val pagingConfig = PagingConfig(
    pageSize = ArticleRemoteMediator.PAGE_SIZE,
    prefetchDistance = 15,
    enablePlaceholders = false
)

@OptIn(ExperimentalPagingApi::class)
class ArticleRepositoryImpl(
    private val api: ArticleApi,
    private val database: AppDatabase,
    private val articleDao: ArticleDao,
    private val remoteKeysDao: RemoteKeysDao,
    private val performanceTracer: PerformanceTracer? = null,
    private val analyticsRepository: AnalyticsRepository? = null,
) : ArticleRepository {

    override fun getArticles(): Flow<PagingData<Article>> =
        Pager(
            config = pagingConfig,
            remoteMediator = ArticleRemoteMediator(
                api = api,
                database = database,
                articleDao = articleDao,
                remoteKeysDao = remoteKeysDao,
                performanceTracer = performanceTracer,
                analyticsRepository = analyticsRepository,
            ),
            pagingSourceFactory = { articleDao.pagingSource() }
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }

    override fun searchArticles(query: String): Flow<PagingData<Article>> {
        val ftsQuery = buildFtsQuery(query)
        return Pager(
            config = pagingConfig,
            remoteMediator = SearchRemoteMediator(query, api, articleDao),
            pagingSourceFactory = { articleDao.searchPagingSource(ftsQuery) }
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
    }

    override suspend fun getArticleDetail(id: Int): Result<Article> {
        val cached = articleDao.getById(id)
        if (cached != null) return Result.success(cached.toDomain())

        return when (val result = api.getArticle(id)) {
            is NetworkResult.Success -> {
                val entity = result.data.toEntity()
                articleDao.insertAll(listOf(entity))
                Result.success(entity.toDomain())
            }
            is NetworkResult.HttpError -> {
                if (result.code == 404) Result.failure(ArticleNotFoundException(id))
                else Result.failure(Exception("HTTP ${result.code}: ${result.message}"))
            }
            is NetworkResult.NetworkError -> Result.failure(result.cause)
            is NetworkResult.UnknownError -> Result.failure(result.cause)
        }
    }

    override fun isDataStale(ttlMinutes: Int): Boolean {
        val lastFetched = kotlinx.coroutines.runBlocking { remoteKeysDao.getLastFetchedAt() }
            ?: return true
        val ttlMillis = TimeUnit.MINUTES.toMillis(ttlMinutes.toLong())
        return System.currentTimeMillis() - lastFetched > ttlMillis
    }

    override suspend fun getLastSyncedAt(): Instant? =
        remoteKeysDao.getLastFetchedAt()?.let { Instant.ofEpochMilli(it) }

    internal companion object {
        fun buildFtsQuery(query: String): String =
            query.trim().split("\\s+".toRegex())
                .filter { it.isNotEmpty() }
                .joinToString(" ") { "$it*" }
                .ifEmpty { query }
    }
}
