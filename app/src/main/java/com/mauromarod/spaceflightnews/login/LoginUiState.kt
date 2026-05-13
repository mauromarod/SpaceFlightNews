package com.mauromarod.spaceflightnews.login

import androidx.annotation.StringRes

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Error(@param:StringRes val messageRes: Int) : LoginUiState
}

sealed interface LoginUiEffect {
    data object NavigateToNews : LoginUiEffect
}
