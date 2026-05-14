package com.mauromarod.spaceflightnews.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mauromarod.spaceflightnews.core.database.dao.ArticleDao
import com.mauromarod.spaceflightnews.core.database.dao.RemoteKeysDao
import com.mauromarod.spaceflightnews.core.database.entity.ArticleEntity
import com.mauromarod.spaceflightnews.core.database.entity.ArticleFts
import com.mauromarod.spaceflightnews.core.database.entity.RemoteKeysEntity

@Database(
    entities = [ArticleEntity::class, RemoteKeysEntity::class, ArticleFts::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun remoteKeysDao(): RemoteKeysDao
}
