package com.mauromarod.spaceflightnews.core.domain.repository

interface CrashReporter {
    fun recordNonFatal(
        throwable: Throwable,
        extras: Map<String, String> = emptyMap(),
    )

    fun log(message: String)

    fun setUserId(uid: String)
}
