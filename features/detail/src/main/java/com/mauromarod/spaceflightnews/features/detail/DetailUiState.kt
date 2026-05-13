package com.mauromarod.spaceflightnews.features.detail

import com.mauromarod.spaceflightnews.core.domain.model.Article

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Content(val article: Article) : DetailUiState
    data class Error(val message: String, val isNotFound: Boolean) : DetailUiState
}
