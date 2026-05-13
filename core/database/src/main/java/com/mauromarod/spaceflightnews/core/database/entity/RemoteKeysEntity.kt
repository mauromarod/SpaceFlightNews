package com.mauromarod.spaceflightnews.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeysEntity(
    @PrimaryKey val articleId: Int,
    val prevKey: Int?,
    val nextKey: Int?,
    val lastFetchedAt: Long? = null
)
