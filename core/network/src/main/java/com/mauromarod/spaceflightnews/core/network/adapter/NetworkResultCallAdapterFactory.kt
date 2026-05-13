package com.mauromarod.spaceflightnews.core.network.adapter

import com.mauromarod.spaceflightnews.core.network.NetworkResult
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class NetworkResultCallAdapterFactory private constructor() : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) return null
        check(returnType is ParameterizedType) { "Call return type must be parameterized" }

        val callInnerType = getParameterUpperBound(0, returnType)
        if (getRawType(callInnerType) != NetworkResult::class.java) return null
        check(callInnerType is ParameterizedType) { "NetworkResult must be parameterized" }

        val resultInnerType = getParameterUpperBound(0, callInnerType)
        return NetworkResultCallAdapter<Any>(resultInnerType)
    }

    companion object {
        fun create(): NetworkResultCallAdapterFactory = NetworkResultCallAdapterFactory()
    }
}
