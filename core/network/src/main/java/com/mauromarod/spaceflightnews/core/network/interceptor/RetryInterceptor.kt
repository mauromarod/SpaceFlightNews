package com.mauromarod.spaceflightnews.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

private const val MAX_RETRIES = 3
private const val BASE_DELAY_MS = 1000L

class RetryInterceptor(
    private val onMaxRetriesExhausted: ((url: String, code: Int) -> Unit)? = null,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var response = chain.proceed(chain.request())

        while (shouldRetry(response.code) && attempt < MAX_RETRIES) {
            response.close()
            Thread.sleep(BASE_DELAY_MS shl attempt)
            attempt++
            response = chain.proceed(chain.request())
        }

        if (shouldRetry(response.code)) {
            onMaxRetriesExhausted?.invoke(chain.request().url.toString(), response.code)
        }

        return response
    }

    private fun shouldRetry(code: Int): Boolean = code == 429 || code in 500..599
}
