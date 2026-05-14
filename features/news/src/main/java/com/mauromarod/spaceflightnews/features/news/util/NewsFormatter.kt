package com.mauromarod.spaceflightnews.features.news.util

import com.mauromarod.spaceflightnews.features.news.R
import java.time.Instant
import java.time.temporal.ChronoUnit

fun buildOfflineMessage(context: android.content.Context, lastSyncedAt: Instant?): String {
    if (lastSyncedAt == null) return context.getString(R.string.offline_cached)
    val minutesAgo = ChronoUnit.MINUTES.between(lastSyncedAt, Instant.now())
    return when {
        minutesAgo < 1  -> context.getString(R.string.offline_just_now)
        minutesAgo < 60 -> context.getString(R.string.offline_minutes_ago, minutesAgo.toInt())
        else            -> context.getString(R.string.offline_hours_ago, ChronoUnit.HOURS.between(lastSyncedAt, Instant.now()).toInt())
    }
}
