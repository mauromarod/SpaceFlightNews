package com.mauromarod.spaceflightnews.core.domain.usecase

import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.core.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow

class SearchArticlesUseCase(private val repository: ArticleRepository) {
    operator fun invoke(query: String): Flow<List<Article>> = repository.observeSearchedArticles(query)
}
