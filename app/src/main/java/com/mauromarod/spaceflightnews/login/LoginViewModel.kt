package com.mauromarod.spaceflightnews.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauromarod.spaceflightnews.R
import com.mauromarod.spaceflightnews.core.domain.repository.AnalyticsRepository
import com.mauromarod.spaceflightnews.core.domain.repository.AuthRepository
import com.mauromarod.spaceflightnews.core.domain.repository.RemoteConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val remoteConfigRepository: RemoteConfigRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<LoginUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<LoginUiEffect> = _uiEffect.receiveAsFlow()

    fun signInAnonymously() {
        if (_uiState.value is LoginUiState.Loading) return
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            authRepository.signInAnonymously()
                .onSuccess {
                    analyticsRepository.trackLogin("anonymous")
                    remoteConfigRepository.fetchAndActivate()
                    _uiEffect.send(LoginUiEffect.NavigateToNews)
                }
                .onFailure { _uiState.value = LoginUiState.Error(it.toSignInErrorRes()) }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (_uiState.value is LoginUiState.Loading) return
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error(R.string.login_error_fields_required)
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            authRepository.signInWithEmail(email, password)
                .onSuccess {
                    analyticsRepository.trackLogin("email")
                    remoteConfigRepository.fetchAndActivate()
                    _uiEffect.send(LoginUiEffect.NavigateToNews)
                }
                .onFailure { _uiState.value = LoginUiState.Error(it.toSignInErrorRes()) }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        if (_uiState.value is LoginUiState.Loading) return
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error(R.string.login_error_fields_required)
            return
        }
        if (password.length < 6) {
            _uiState.value = LoginUiState.Error(R.string.login_error_password_too_short)
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            authRepository.signUpWithEmail(email, password)
                .onSuccess {
                    analyticsRepository.trackLogin("email")
                    remoteConfigRepository.fetchAndActivate()
                    _uiEffect.send(LoginUiEffect.NavigateToNews)
                }
                .onFailure { _uiState.value = LoginUiState.Error(it.toSignUpErrorRes()) }
        }
    }

    fun clearError() {
        if (_uiState.value is LoginUiState.Error) _uiState.value = LoginUiState.Idle
    }
}

private fun Throwable.toSignInErrorRes(): Int = when {
    message?.contains("INVALID_LOGIN_CREDENTIALS") == true ||
    message?.contains("no user record") == true -> R.string.login_error_invalid_credentials
    message?.contains("network") == true -> R.string.login_error_no_internet
    else -> R.string.login_error_signin_failed
}

private fun Throwable.toSignUpErrorRes(): Int = when {
    message?.contains("EMAIL_EXISTS") == true ||
    message?.contains("email address is already in use") == true -> R.string.login_error_email_in_use
    message?.contains("INVALID_EMAIL") == true ||
    message?.contains("badly formatted") == true -> R.string.login_error_invalid_email
    message?.contains("network") == true -> R.string.login_error_no_internet
    else -> R.string.login_error_signup_failed
}
