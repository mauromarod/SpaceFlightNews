package com.mauromarod.spaceflightnews.core.domain.usecase

import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.core.domain.repository.ArticleRepository
import com.mauromarod.spaceflightnews.core.domain.repository.RemoteConfigRepository

class GetArticleDetailUseCase(
    private val repository: ArticleRepository,
    private val remoteConfig: RemoteConfigRepository,
) {
    suspend operator fun invoke(id: Int): Result<Article> {
        if (repository.isDataStale(remoteConfig.getCacheTtlMinutes())) {
            // Stale data — caller should trigger a refresh.
            // Current implementation serves cached data and relies on
            // the pull-to-refresh or next page load to update the cache.
        }
        return repository.getArticleDetail(id)
    }
}
