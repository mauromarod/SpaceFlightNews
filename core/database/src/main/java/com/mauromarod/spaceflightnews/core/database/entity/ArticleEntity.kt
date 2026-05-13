package com.mauromarod.spaceflightnews.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val summary: String,
    val imageUrl: String?,
    val newsSite: String,
    val publishedAt: Long,
    val url: String,
    val featured: Boolean
)
