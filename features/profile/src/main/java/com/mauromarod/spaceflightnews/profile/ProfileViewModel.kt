package com.mauromarod.spaceflightnews.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauromarod.spaceflightnews.core.domain.model.ThemePreference
import com.mauromarod.spaceflightnews.core.domain.repository.AnalyticsRepository
import com.mauromarod.spaceflightnews.core.domain.repository.AuthRepository
import com.mauromarod.spaceflightnews.core.domain.repository.RemoteConfigRepository
import com.mauromarod.spaceflightnews.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        authRepository.currentUser
            .onEach { user ->
                _uiState.update { it.copy(user = user) }
                refreshFlags()
            }
            .launchIn(viewModelScope)

        authRepository.currentUser
            .filterNotNull()
            .flatMapLatest { user -> userPreferencesRepository.getThemePreference(user.uid) }
            .onEach { theme -> _uiState.update { it.copy(theme = theme) } }
            .launchIn(viewModelScope)

        remoteConfigRepository.configUpdates
            .onEach { refreshFlags() }
            .launchIn(viewModelScope)
    }

    private fun refreshFlags() {
        _uiState.update {
            it.copy(isThemeEnabled = remoteConfigRepository.isThemeToggleEnabled())
        }
    }

    fun setTheme(theme: ThemePreference) {
        val uid = _uiState.value.user?.uid ?: return
        viewModelScope.launch {
            userPreferencesRepository.setThemePreference(uid, theme)
            analyticsRepository.trackThemeChanged(theme.name.lowercase())
            analyticsRepository.setUserProperty("preferred_theme", theme.name.lowercase())
        }
    }

    fun signOut() {
        authRepository.signOut()
        viewModelScope.launch {
            remoteConfigRepository.fetchAndActivate()
        }
        _uiState.update { it.copy(isSignedOut = true) }
    }
}
