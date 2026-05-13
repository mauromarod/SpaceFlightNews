package com.mauromarod.spaceflightnews.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mauromarod.spaceflightnews.core.domain.model.LanguagePreference
import com.mauromarod.spaceflightnews.core.domain.model.ThemePreference
import com.mauromarod.spaceflightnews.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreUserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    override fun getThemePreference(uid: String): Flow<ThemePreference> =
        dataStore.data.map { prefs ->
            val stored = prefs[themeKey(uid)]
            ThemePreference.entries.firstOrNull { it.name == stored } ?: ThemePreference.SYSTEM
        }

    override fun getLanguagePreference(uid: String): Flow<LanguagePreference> =
        dataStore.data.map { prefs ->
            val stored = prefs[languageKey(uid)]
            LanguagePreference.entries.firstOrNull { it.name == stored } ?: LanguagePreference.SYSTEM
        }

    override suspend fun setThemePreference(uid: String, preference: ThemePreference) {
        dataStore.edit { prefs -> prefs[themeKey(uid)] = preference.name }
    }

    override suspend fun setLanguagePreference(uid: String, preference: LanguagePreference) {
        dataStore.edit { prefs -> prefs[languageKey(uid)] = preference.name }
    }

    private fun themeKey(uid: String) = stringPreferencesKey("theme_$uid")
    private fun languageKey(uid: String) = stringPreferencesKey("language_$uid")
}
