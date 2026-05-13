package com.mauromarod.spaceflightnews.core.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.mauromarod.spaceflightnews.core.data.mapper.toEntity
import com.mauromarod.spaceflightnews.core.database.dao.ArticleDao
import com.mauromarod.spaceflightnews.core.database.entity.ArticleEntity
import com.mauromarod.spaceflightnews.core.network.NetworkResult
import com.mauromarod.spaceflightnews.core.network.api.ArticleApi

@OptIn(ExperimentalPagingApi::class)
internal class SearchRemoteMediator(
    private val query: String,
    private val api: ArticleApi,
    private val articleDao: ArticleDao,
    private val pageSize: Int = ArticleRemoteMediator.PAGE_SIZE
) : RemoteMediator<Int, ArticleEntity>() {

    private var nextOffset = 0

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        val offset = when (loadType) {
            LoadType.REFRESH -> {
                nextOffset = 0
                0
            }
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> nextOffset
        }

        return when (val result = api.searchArticles(query = query, limit = pageSize, offset = offset)) {
            is NetworkResult.Success -> {
                nextOffset = offset + result.data.results.size
                articleDao.insertAll(result.data.results.map { it.toEntity() })
                MediatorResult.Success(endOfPaginationReached = result.data.next == null)
            }
            is NetworkResult.HttpError ->
                MediatorResult.Error(Exception("HTTP ${result.code}: ${result.message}"))
            is NetworkResult.NetworkError -> MediatorResult.Error(result.cause)
            is NetworkResult.UnknownError -> MediatorResult.Error(result.cause)
        }
    }
}
