package com.mauromarod.spaceflightnews.auth

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Error(val messageRes: Int) : LoginUiState
    data object Success : LoginUiState
}
