package com.mauromarod.spaceflightnews.di

import com.mauromarod.spaceflightnews.BuildConfig
import com.mauromarod.spaceflightnews.core.domain.repository.CrashReporter
import com.mauromarod.spaceflightnews.core.network.adapter.NetworkResultCallAdapterFactory
import com.mauromarod.spaceflightnews.core.network.api.ArticleApi
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()

    @Provides
    @Singleton
    fun provideOkHttpClient(crashReporter: CrashReporter): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(
                com.mauromarod.spaceflightnews.core.network.interceptor.RetryInterceptor(
                    onMaxRetriesExhausted = { url, code ->
                        crashReporter.recordNonFatal(
                            Exception("Max retries exhausted: HTTP $code"),
                            extras = mapOf("url" to url, "response_code" to code.toString())
                        )
                    }
                )
            )

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            )
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(NetworkResultCallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideArticleApi(retrofit: Retrofit): ArticleApi =
        retrofit.create(ArticleApi::class.java)
}
