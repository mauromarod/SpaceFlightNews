package com.mauromarod.spaceflightnews.features.news

sealed interface NewsUiState {
    data object Loading : NewsUiState
    data object Content : NewsUiState
    data class Error(val message: String) : NewsUiState
}
