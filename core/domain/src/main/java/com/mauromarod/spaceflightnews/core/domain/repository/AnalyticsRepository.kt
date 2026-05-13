package com.mauromarod.spaceflightnews.core.domain.repository

interface AnalyticsRepository {
    fun trackArticleOpened(articleId: Int, title: String, newsSite: String, source: String)
    fun trackSearchPerformed(queryLength: Int)
    fun trackSearchConverted(articleId: Int)
    fun trackFeedPageLoaded(trigger: String)
    fun trackExternalUrlOpened(articleId: Int)
    fun trackThemeChanged(theme: String)
    fun trackLanguageChanged(language: String)
    fun trackLogin(method: String)
    fun setUserProperty(key: String, value: String)
}
