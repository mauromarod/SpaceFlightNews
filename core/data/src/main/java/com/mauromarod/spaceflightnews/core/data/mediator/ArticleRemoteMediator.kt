package com.mauromarod.spaceflightnews.core.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.mauromarod.spaceflightnews.core.data.mapper.toEntity
import com.mauromarod.spaceflightnews.core.database.AppDatabase
import com.mauromarod.spaceflightnews.core.database.dao.ArticleDao
import com.mauromarod.spaceflightnews.core.database.dao.RemoteKeysDao
import com.mauromarod.spaceflightnews.core.database.entity.ArticleEntity
import com.mauromarod.spaceflightnews.core.database.entity.RemoteKeysEntity
import com.mauromarod.spaceflightnews.core.domain.repository.AnalyticsRepository
import com.mauromarod.spaceflightnews.core.domain.repository.PerformanceTracer
import com.mauromarod.spaceflightnews.core.network.NetworkResult
import com.mauromarod.spaceflightnews.core.network.api.ArticleApi

@OptIn(ExperimentalPagingApi::class)
internal class ArticleRemoteMediator(
    private val api: ArticleApi,
    private val database: AppDatabase,
    private val articleDao: ArticleDao,
    private val remoteKeysDao: RemoteKeysDao,
    private val pageSize: Int = PAGE_SIZE,
    private val performanceTracer: PerformanceTracer? = null,
    private val analyticsRepository: AnalyticsRepository? = null,
) : RemoteMediator<Int, ArticleEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        val offset = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                // null lastItem or missing remoteKey means REFRESH hasn't populated the DB yet —
                // not that we've reached the end. endOfPaginationReached=false lets Paging retry.
                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = false)
                val remoteKey = remoteKeysDao.getByArticleId(lastItem.id)
                    ?: return MediatorResult.Success(endOfPaginationReached = false)
                remoteKey.nextKey ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        if (loadType == LoadType.APPEND) {
            analyticsRepository?.trackFeedPageLoaded("append")
        }

        val trace = performanceTracer?.newTrace("articles_network_fetch")?.also {
            it.putAttribute("load_type", loadType.name)
            it.start()
        }

        return try {
            when (val result = api.getArticles(limit = pageSize, offset = offset)) {
                is NetworkResult.Success -> {
                    val response = result.data
                    val endOfPaginationReached = response.next == null
                    val prevKey = if (offset == 0) null else offset - pageSize
                    val nextKey = if (endOfPaginationReached) null else offset + pageSize
                    val now = System.currentTimeMillis()

                    database.withTransaction {
                        persist(loadType, response.results.map { it.toEntity() }, prevKey, nextKey, now)
                    }

                    trace?.putMetric("articles_count", response.results.size.toLong())
                    MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
                }
                is NetworkResult.HttpError ->
                    MediatorResult.Error(Exception("HTTP ${result.code}: ${result.message}"))
                is NetworkResult.NetworkError -> MediatorResult.Error(result.cause)
                is NetworkResult.UnknownError -> MediatorResult.Error(result.cause)
            }
        } finally {
            trace?.stop()
        }
    }

    internal suspend fun persist(
        loadType: LoadType,
        articles: List<ArticleEntity>,
        prevKey: Int?,
        nextKey: Int?,
        now: Long
    ) {
        if (loadType == LoadType.REFRESH) {
            remoteKeysDao.clearAll()
            articleDao.clearAll()
        }
        articleDao.insertAll(articles)
        remoteKeysDao.insertAll(
            articles.map { entity ->
                RemoteKeysEntity(
                    articleId = entity.id,
                    prevKey = prevKey,
                    nextKey = nextKey,
                    lastFetchedAt = if (loadType == LoadType.REFRESH) now else null
                )
            }
        )
    }

    companion object {
        const val PAGE_SIZE = 32
    }
}
