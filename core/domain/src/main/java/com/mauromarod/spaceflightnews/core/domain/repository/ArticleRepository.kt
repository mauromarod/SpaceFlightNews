package com.mauromarod.spaceflightnews.core.domain.repository

import com.mauromarod.spaceflightnews.core.domain.model.Article
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface ArticleRepository {
    fun observeArticles(): Flow<List<Article>>

    fun observeSearchedArticles(query: String): Flow<List<Article>>

    suspend fun getArticleDetail(id: Int): Result<Article>

    fun isDataStale(ttlMinutes: Int): Boolean

    suspend fun getLastSyncedAt(): Instant?
}
