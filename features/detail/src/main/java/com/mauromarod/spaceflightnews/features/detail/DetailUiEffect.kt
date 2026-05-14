package com.mauromarod.spaceflightnews.features.detail

sealed interface DetailUiEffect {
    data class OpenUrl(val url: String) : DetailUiEffect
}
