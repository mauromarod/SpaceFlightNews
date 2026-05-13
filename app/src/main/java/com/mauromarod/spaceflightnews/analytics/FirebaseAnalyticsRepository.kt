package com.mauromarod.spaceflightnews.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.mauromarod.spaceflightnews.core.domain.repository.AnalyticsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsRepository @Inject constructor(
    private val analytics: FirebaseAnalytics,
) : AnalyticsRepository {

    override fun trackArticleOpened(articleId: Int, title: String, newsSite: String, source: String) {
        analytics.logEvent("article_opened", Bundle().apply {
            putInt("article_id", articleId)
            putString("article_title", title.take(40))
            putString("news_site", newsSite)
            putString("source", source)
        })
    }

    override fun trackSearchPerformed(queryLength: Int) {
        analytics.logEvent("search_performed", Bundle().apply {
            putInt("query_length", queryLength)
        })
    }

    override fun trackSearchConverted(articleId: Int) {
        analytics.logEvent("search_converted", Bundle().apply {
            putInt("article_id", articleId)
        })
    }

    override fun trackFeedPageLoaded(trigger: String) {
        analytics.logEvent("feed_page_loaded", Bundle().apply {
            putString("trigger", trigger)
        })
    }

    override fun trackExternalUrlOpened(articleId: Int) {
        analytics.logEvent("external_url_opened", Bundle().apply {
            putInt("article_id", articleId)
        })
    }

    override fun trackThemeChanged(theme: String) {
        analytics.logEvent("theme_changed", Bundle().apply {
            putString("theme", theme)
        })
    }

    override fun trackLanguageChanged(language: String) {
        analytics.logEvent("language_changed", Bundle().apply {
            putString("language", language)
        })
    }

    override fun trackLogin(method: String) {
        analytics.logEvent("login", Bundle().apply {
            putString("method", method)
        })
    }

    override fun setUserProperty(key: String, value: String) {
        analytics.setUserProperty(key, value)
    }
}
