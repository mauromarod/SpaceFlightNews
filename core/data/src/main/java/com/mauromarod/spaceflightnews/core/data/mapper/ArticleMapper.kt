package com.mauromarod.spaceflightnews.core.data.mapper

import com.mauromarod.spaceflightnews.core.database.entity.ArticleEntity
import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.core.network.dto.ArticleDto
import java.time.Instant

fun ArticleDto.toDomain(): Article = Article(
    id = id,
    title = title,
    summary = summary,
    imageUrl = imageUrl?.takeIf { it.isNotBlank() },
    newsSite = newsSite,
    publishedAt = Instant.parse(publishedAt),
    url = url,
    featured = featured
)

fun ArticleDto.toEntity(): ArticleEntity = ArticleEntity(
    id = id,
    title = title,
    summary = summary,
    imageUrl = imageUrl?.takeIf { it.isNotBlank() },
    newsSite = newsSite,
    publishedAt = Instant.parse(publishedAt).toEpochMilli(),
    url = url,
    featured = featured
)
