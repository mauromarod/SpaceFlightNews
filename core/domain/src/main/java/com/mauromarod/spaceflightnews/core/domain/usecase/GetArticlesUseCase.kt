package com.mauromarod.spaceflightnews.core.domain.usecase

import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.core.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow

class GetArticlesUseCase(private val repository: ArticleRepository) {
    operator fun invoke(query: String): Flow<List<Article>> =
        if (query.isBlank()) repository.observeArticles()
        else repository.observeSearchedArticles(query)
}
