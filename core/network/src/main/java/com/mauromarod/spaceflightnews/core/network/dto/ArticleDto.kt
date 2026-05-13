package com.mauromarod.spaceflightnews.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ArticleDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "url") val url: String,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "news_site") val newsSite: String,
    @Json(name = "summary") val summary: String,
    @Json(name = "published_at") val publishedAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "featured") val featured: Boolean,
    @Json(name = "launches") val launches: List<LaunchRefDto>,
    @Json(name = "events") val events: List<EventRefDto>
)
