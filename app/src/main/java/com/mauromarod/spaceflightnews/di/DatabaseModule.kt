package com.mauromarod.spaceflightnews.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mauromarod.spaceflightnews.core.database.AppDatabase
import com.mauromarod.spaceflightnews.core.database.dao.ArticleDao
import com.mauromarod.spaceflightnews.core.database.dao.RemoteKeysDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE VIRTUAL TABLE IF NOT EXISTS `articles_fts` USING fts4(content=`articles`, `title`, `summary`)"
        )
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_articles_publishedAt` ON `articles` (`publishedAt`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_remote_keys_lastFetchedAt` ON `remote_keys` (`lastFetchedAt`)")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "spaceflight_news.db")
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides
    fun provideArticleDao(db: AppDatabase): ArticleDao = db.articleDao()

    @Provides
    fun provideRemoteKeysDao(db: AppDatabase): RemoteKeysDao = db.remoteKeysDao()
}
