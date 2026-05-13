package com.mauromarod.spaceflightnews.core.network.adapter

import com.mauromarod.spaceflightnews.core.network.NetworkResult
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

internal class NetworkResultCallAdapter<T>(
    private val responseType: Type
) : CallAdapter<T, Call<NetworkResult<T>>> {

    override fun responseType(): Type = responseType

    override fun adapt(call: Call<T>): Call<NetworkResult<T>> = NetworkResultCall(call)
}
