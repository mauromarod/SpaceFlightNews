package com.mauromarod.spaceflightnews.core.common.ext

import java.io.IOException

fun Throwable.toFriendlyMessage(): String =
    if (this is IOException) "No internet connection" else message ?: "Something went wrong"
