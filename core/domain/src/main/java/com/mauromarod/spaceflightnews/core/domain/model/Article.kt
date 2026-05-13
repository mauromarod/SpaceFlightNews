package com.mauromarod.spaceflightnews.core.domain.model

import java.time.Instant

data class Article(
    val id: Int,
    val title: String,
    val summary: String,
    val imageUrl: String?,
    val newsSite: String,
    val publishedAt: Instant,
    val url: String,
    val featured: Boolean,
)
