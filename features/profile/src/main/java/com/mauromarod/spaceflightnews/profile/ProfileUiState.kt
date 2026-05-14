package com.mauromarod.spaceflightnews.profile

import com.mauromarod.spaceflightnews.core.domain.model.AuthUser
import com.mauromarod.spaceflightnews.core.domain.model.ThemePreference

data class ProfileUiState(
    val user: AuthUser? = null,
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val isThemeEnabled: Boolean = false,
    val isSignedOut: Boolean = false,
)
