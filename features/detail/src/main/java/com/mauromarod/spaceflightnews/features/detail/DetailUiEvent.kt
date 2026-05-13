package com.mauromarod.spaceflightnews.features.detail

sealed interface DetailUiEvent {
    data object OpenUrlClicked : DetailUiEvent
    data object RetryClicked : DetailUiEvent
    data object BackClicked : DetailUiEvent
}
