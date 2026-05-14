package com.mauromarod.spaceflightnews.core.network.api

import com.mauromarod.spaceflightnews.core.network.NetworkResult
import com.mauromarod.spaceflightnews.core.network.dto.ArticleDto
import com.mauromarod.spaceflightnews.core.network.dto.ArticleListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ArticleApi {

    @GET("articles/")
    suspend fun getArticles(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("ordering") ordering: String = DEFAULT_ORDERING,
    ): NetworkResult<ArticleListResponseDto>

    @GET("articles/")
    suspend fun searchArticles(
        @Query("search") query: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("ordering") ordering: String = DEFAULT_ORDERING,
    ): NetworkResult<ArticleListResponseDto>

    @GET("articles/{id}/")
    suspend fun getArticle(
        @Path("id") id: Int
    ): NetworkResult<ArticleDto>

    companion object {
        private const val DEFAULT_ORDERING = "-published_at"
    }
}
