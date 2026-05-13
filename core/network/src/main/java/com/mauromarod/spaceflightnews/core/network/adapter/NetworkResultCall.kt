package com.mauromarod.spaceflightnews.core.network.adapter

import com.mauromarod.spaceflightnews.core.network.NetworkResult
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class NetworkResultCall<T>(
    private val delegate: Call<T>
) : Call<NetworkResult<T>> {

    override fun enqueue(callback: Callback<NetworkResult<T>>) {
        delegate.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val networkResult = if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        NetworkResult.Success(body)
                    } else {
                        NetworkResult.HttpError(response.code(), "Empty response body")
                    }
                } else {
                    NetworkResult.HttpError(response.code(), response.message())
                }
                callback.onResponse(this@NetworkResultCall, Response.success(networkResult))
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                val networkResult = if (t is java.io.IOException) {
                    NetworkResult.NetworkError(t)
                } else {
                    NetworkResult.UnknownError(t)
                }
                callback.onResponse(this@NetworkResultCall, Response.success(networkResult))
            }
        })
    }

    override fun execute(): Response<NetworkResult<T>> =
        throw UnsupportedOperationException("NetworkResultCall does not support sync execution")
    override fun clone(): Call<NetworkResult<T>> = NetworkResultCall(delegate.clone())
    override fun request(): Request = delegate.request()
    override fun timeout(): Timeout = delegate.timeout()
    override fun isExecuted(): Boolean = delegate.isExecuted
    override fun isCanceled(): Boolean = delegate.isCanceled
    override fun cancel() = delegate.cancel()
}
