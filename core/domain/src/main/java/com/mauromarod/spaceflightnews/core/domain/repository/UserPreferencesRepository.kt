package com.mauromarod.spaceflightnews.core.domain.repository

import com.mauromarod.spaceflightnews.core.domain.model.LanguagePreference
import com.mauromarod.spaceflightnews.core.domain.model.ThemePreference
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getThemePreference(uid: String): Flow<ThemePreference>

    fun getLanguagePreference(uid: String): Flow<LanguagePreference>

    suspend fun setThemePreference(
        uid: String,
        preference: ThemePreference,
    )

    suspend fun setLanguagePreference(
        uid: String,
        preference: LanguagePreference,
    )
}
