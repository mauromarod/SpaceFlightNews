package com.mauromarod.spaceflightnews.di

import com.mauromarod.spaceflightnews.core.data.repository.ArticleRepositoryImpl
import com.mauromarod.spaceflightnews.core.database.AppDatabase
import com.mauromarod.spaceflightnews.core.database.dao.ArticleDao
import com.mauromarod.spaceflightnews.core.database.dao.RemoteKeysDao
import com.mauromarod.spaceflightnews.core.domain.repository.AnalyticsRepository
import com.mauromarod.spaceflightnews.core.domain.repository.ArticleRepository
import com.mauromarod.spaceflightnews.core.domain.repository.PerformanceTracer
import com.mauromarod.spaceflightnews.core.network.api.ArticleApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideArticleRepository(
        api: ArticleApi,
        database: AppDatabase,
        articleDao: ArticleDao,
        remoteKeysDao: RemoteKeysDao,
        performanceTracer: PerformanceTracer,
        analyticsRepository: AnalyticsRepository,
    ): ArticleRepository = ArticleRepositoryImpl(
        api = api,
        database = database,
        articleDao = articleDao,
        remoteKeysDao = remoteKeysDao,
        performanceTracer = performanceTracer,
        analyticsRepository = analyticsRepository,
    )
}
