package com.mauromarod.spaceflightnews.core.data.util

internal fun buildFtsQuery(query: String): String =
    query.trim().split("\\s+".toRegex())
        .filter { it.isNotEmpty() }
        .joinToString(" ") { "$it*" }
        .ifEmpty { query }
