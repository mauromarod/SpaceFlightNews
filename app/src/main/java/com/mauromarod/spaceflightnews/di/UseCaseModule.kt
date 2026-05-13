package com.mauromarod.spaceflightnews.di

import com.mauromarod.spaceflightnews.core.domain.repository.ArticleRepository
import com.mauromarod.spaceflightnews.core.domain.repository.RemoteConfigRepository
import com.mauromarod.spaceflightnews.core.domain.usecase.GetArticleDetailUseCase
import com.mauromarod.spaceflightnews.core.domain.usecase.GetArticlesUseCase
import com.mauromarod.spaceflightnews.core.domain.usecase.SearchArticlesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @ViewModelScoped
    fun provideGetArticlesUseCase(repository: ArticleRepository): GetArticlesUseCase =
        GetArticlesUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideSearchArticlesUseCase(repository: ArticleRepository): SearchArticlesUseCase =
        SearchArticlesUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideGetArticleDetailUseCase(
        repository: ArticleRepository,
        remoteConfig: RemoteConfigRepository,
    ): GetArticleDetailUseCase = GetArticleDetailUseCase(
        repository = repository,
        remoteConfig = remoteConfig,
    )
}
