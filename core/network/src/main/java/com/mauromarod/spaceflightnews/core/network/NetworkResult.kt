package com.mauromarod.spaceflightnews.core.network

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class HttpError(val code: Int, val message: String) : NetworkResult<Nothing>()
    data class NetworkError(val cause: Throwable) : NetworkResult<Nothing>()
    data class UnknownError(val cause: Throwable) : NetworkResult<Nothing>()
}
