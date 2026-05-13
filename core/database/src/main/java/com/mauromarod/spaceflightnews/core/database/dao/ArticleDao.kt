package com.mauromarod.spaceflightnews.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mauromarod.spaceflightnews.core.database.entity.ArticleEntity

@Dao
interface ArticleDao {

    @Query("SELECT * FROM articles ORDER BY publishedAt DESC")
    fun pagingSource(): PagingSource<Int, ArticleEntity>

    @Query(
        """SELECT articles.* FROM articles
           INNER JOIN articles_fts ON articles.rowid = articles_fts.rowid
           WHERE articles_fts MATCH :query
           ORDER BY articles.publishedAt DESC"""
    )
    fun searchPagingSource(query: String): PagingSource<Int, ArticleEntity>

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getById(id: Int): ArticleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<ArticleEntity>)

    @Query("DELETE FROM articles")
    suspend fun clearAll()
}
