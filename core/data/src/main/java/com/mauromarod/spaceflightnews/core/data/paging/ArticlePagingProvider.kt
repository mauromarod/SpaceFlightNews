package com.mauromarod.spaceflightnews.core.data.paging

import androidx.paging.PagingData
import com.mauromarod.spaceflightnews.core.domain.model.Article
import kotlinx.coroutines.flow.Flow

interface ArticlePagingProvider {
    fun observeArticleFeed(): Flow<PagingData<Article>>

    fun observeArticleSearch(query: String): Flow<PagingData<Article>>
}
