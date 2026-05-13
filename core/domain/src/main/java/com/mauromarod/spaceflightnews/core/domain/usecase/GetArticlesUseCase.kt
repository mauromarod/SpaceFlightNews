package com.mauromarod.spaceflightnews.core.domain.usecase

import androidx.paging.PagingData
import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.core.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow

class GetArticlesUseCase(private val repository: ArticleRepository) {
    operator fun invoke(): Flow<PagingData<Article>> = repository.getArticles()
}
