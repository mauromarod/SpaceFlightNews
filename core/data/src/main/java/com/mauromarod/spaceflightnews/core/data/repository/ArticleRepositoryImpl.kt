package com.mauromarod.spaceflightnews.core.data.repository

import com.mauromarod.spaceflightnews.core.data.mapper.toEntity
import com.mauromarod.spaceflightnews.core.database.dao.ArticleDao
import com.mauromarod.spaceflightnews.core.database.dao.RemoteKeysDao
import com.mauromarod.spaceflightnews.core.database.mapper.toDomain
import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.core.domain.model.ArticleNotFoundException
import com.mauromarod.spaceflightnews.core.domain.repository.ArticleRepository
import com.mauromarod.spaceflightnews.core.network.NetworkResult
import com.mauromarod.spaceflightnews.core.network.api.ArticleApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.concurrent.TimeUnit

class ArticleRepositoryImpl(
    private val api: ArticleApi,
    private val articleDao: ArticleDao,
    private val remoteKeysDao: RemoteKeysDao,
) : ArticleRepository {

    override fun observeArticles(): Flow<List<Article>> =
        articleDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeSearchedArticles(query: String): Flow<List<Article>> {
        val ftsQuery = buildFtsQuery(query)
        return articleDao.observeSearch(ftsQuery).map { entities ->
            entities.map { it.toDomain() }
        }
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
