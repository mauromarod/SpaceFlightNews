package com.mauromarod.spaceflightnews.core.domain.usecase

import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.core.domain.repository.ArticleRepository

class GetArticleDetailUseCase(private val repository: ArticleRepository) {
    suspend operator fun invoke(id: Int): Result<Article> = repository.getArticleDetail(id)
}
