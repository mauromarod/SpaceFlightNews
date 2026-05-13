package com.mauromarod.spaceflightnews.features.news

sealed interface NewsUiEffect {
    data class NavigateToDetail(val articleId: Int) : NewsUiEffect
    data class ShowSnackbar(val message: String) : NewsUiEffect
}
