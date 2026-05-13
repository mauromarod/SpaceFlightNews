package com.mauromarod.spaceflightnews.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mauromarod.spaceflightnews.core.database.entity.RemoteKeysEntity

@Dao
interface RemoteKeysDao {

    @Query("SELECT * FROM remote_keys WHERE articleId = :articleId")
    suspend fun getByArticleId(articleId: Int): RemoteKeysEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(keys: List<RemoteKeysEntity>)

    @Query("SELECT MAX(lastFetchedAt) FROM remote_keys")
    suspend fun getLastFetchedAt(): Long?

    @Query("DELETE FROM remote_keys")
    suspend fun clearAll()
}
