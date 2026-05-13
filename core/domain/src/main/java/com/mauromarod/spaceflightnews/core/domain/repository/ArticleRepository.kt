package com.mauromarod.spaceflightnews.core.domain.repository

import androidx.paging.PagingData
import com.mauromarod.spaceflightnews.core.domain.model.Article
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface ArticleRepository {
    fun getArticles(): Flow<PagingData<Article>>

    fun searchArticles(query: String): Flow<PagingData<Article>>

    suspend fun getArticleDetail(id: Int): Result<Article>

    fun isDataStale(ttlMinutes: Int = 5): Boolean

    suspend fun getLastSyncedAt(): Instant?
}
