package com.mauromarod.spaceflightnews.di

import com.mauromarod.spaceflightnews.core.data.paging.ArticlePagingProvider
import com.mauromarod.spaceflightnews.core.data.paging.ArticlePagingProviderImpl
import com.mauromarod.spaceflightnews.core.data.repository.ArticleRepositoryImpl
import com.mauromarod.spaceflightnews.core.database.AppDatabase
import com.mauromarod.spaceflightnews.core.database.dao.ArticleDao
import com.mauromarod.spaceflightnews.core.database.dao.RemoteKeysDao
import android.content.Context
import com.mauromarod.spaceflightnews.core.domain.repository.AnalyticsRepository
import com.mauromarod.spaceflightnews.core.domain.repository.ArticleRepository
import com.mauromarod.spaceflightnews.core.domain.repository.PerformanceTracer
import com.mauromarod.spaceflightnews.core.network.api.ArticleApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideArticleRepository(
        articleDao: ArticleDao,
        remoteKeysDao: RemoteKeysDao,
        api: ArticleApi,
    ): ArticleRepository = ArticleRepositoryImpl(
        articleDao = articleDao,
        remoteKeysDao = remoteKeysDao,
        api = api,
    )

    @Provides
    @Singleton
    fun provideArticlePagingProvider(
        @ApplicationContext context: Context,
        api: ArticleApi,
        database: AppDatabase,
        articleDao: ArticleDao,
        remoteKeysDao: RemoteKeysDao,
        performanceTracer: PerformanceTracer,
        analyticsRepository: AnalyticsRepository,
    ): ArticlePagingProvider = ArticlePagingProviderImpl(
        context = context,
        api = api,
        database = database,
        articleDao = articleDao,
        remoteKeysDao = remoteKeysDao,
        performanceTracer = performanceTracer,
        analyticsRepository = analyticsRepository,
    )
}
