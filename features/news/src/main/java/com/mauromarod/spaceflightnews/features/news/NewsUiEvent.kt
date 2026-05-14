package com.mauromarod.spaceflightnews.features.news

sealed interface NewsUiEvent {
    data class SearchQueryChanged(val query: String) : NewsUiEvent
    data class ArticleTapped(val articleId: Int) : NewsUiEvent
}
