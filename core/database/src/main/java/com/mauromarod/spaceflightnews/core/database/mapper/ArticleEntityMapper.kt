package com.mauromarod.spaceflightnews.core.database.mapper

import com.mauromarod.spaceflightnews.core.database.entity.ArticleEntity
import com.mauromarod.spaceflightnews.core.domain.model.Article
import java.time.Instant

fun ArticleEntity.toDomain(): Article = Article(
    id = id,
    title = title,
    summary = summary,
    imageUrl = imageUrl,
    newsSite = newsSite,
    publishedAt = Instant.ofEpochMilli(publishedAt),
    url = url,
    featured = featured
)

fun Article.toEntity(): ArticleEntity = ArticleEntity(
    id = id,
    title = title,
    summary = summary,
    imageUrl = imageUrl,
    newsSite = newsSite,
    publishedAt = publishedAt.toEpochMilli(),
    url = url,
    featured = featured
)
