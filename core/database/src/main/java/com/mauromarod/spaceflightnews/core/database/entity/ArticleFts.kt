package com.mauromarod.spaceflightnews.core.database.entity

import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "articles_fts")
@Fts4(contentEntity = ArticleEntity::class)
data class ArticleFts(
    val title: String,
    val summary: String
)
