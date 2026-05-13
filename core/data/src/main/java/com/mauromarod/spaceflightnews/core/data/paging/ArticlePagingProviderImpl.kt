package com.mauromarod.spaceflightnews.core.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.mauromarod.spaceflightnews.core.data.mediator.ArticleRemoteMediator
import com.mauromarod.spaceflightnews.core.data.mediator.ArticleRemoteMediator.Companion.PAGE_SIZE
import com.mauromarod.spaceflightnews.core.data.mediator.ArticleRemoteMediator.Companion.PREFETCH_DISTANCE
import com.mauromarod.spaceflightnews.core.data.mediator.SearchRemoteMediator
import com.mauromarod.spaceflightnews.core.database.dao.ArticleDao
import com.mauromarod.spaceflightnews.core.database.dao.RemoteKeysDao
import com.mauromarod.spaceflightnews.core.database.mapper.toDomain
import com.mauromarod.spaceflightnews.core.database.AppDatabase
import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.core.domain.repository.AnalyticsRepository
import com.mauromarod.spaceflightnews.core.domain.repository.PerformanceTracer
import com.mauromarod.spaceflightnews.core.network.api.ArticleApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val pagingConfig = PagingConfig(
    pageSize = PAGE_SIZE,
    prefetchDistance = PREFETCH_DISTANCE,
)

@OptIn(ExperimentalPagingApi::class)
class ArticlePagingProviderImpl(
    private val api: ArticleApi,
    private val database: AppDatabase,
    private val articleDao: ArticleDao,
    private val remoteKeysDao: RemoteKeysDao,
    private val performanceTracer: PerformanceTracer? = null,
    private val analyticsRepository: AnalyticsRepository? = null,
) : ArticlePagingProvider {

    override fun observeArticleFeed(): Flow<PagingData<Article>> =
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

    override fun observeArticleSearch(query: String): Flow<PagingData<Article>> {
        val ftsQuery = buildFtsQuery(query)
        return Pager(
            config = pagingConfig,
            remoteMediator = SearchRemoteMediator(query, api, articleDao),
            pagingSourceFactory = { articleDao.searchPagingSource(ftsQuery) }
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
    }

    internal companion object {
        fun buildFtsQuery(query: String): String =
            query.trim().split("\\s+".toRegex())
                .filter { it.isNotEmpty() }
                .joinToString(" ") { "$it*" }
                .ifEmpty { query }
    }
}
