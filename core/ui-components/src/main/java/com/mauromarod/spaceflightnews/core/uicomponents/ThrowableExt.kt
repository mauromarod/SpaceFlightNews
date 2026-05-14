package com.mauromarod.spaceflightnews.core.uicomponents

import java.io.IOException

fun Throwable.toFriendlyMessageRes(): Int =
    if (this is IOException) R.string.error_no_internet else R.string.error_unknown
