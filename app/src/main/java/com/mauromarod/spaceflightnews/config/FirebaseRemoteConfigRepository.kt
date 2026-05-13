package com.mauromarod.spaceflightnews.config

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.mauromarod.spaceflightnews.BuildConfig
import com.mauromarod.spaceflightnews.core.domain.repository.RemoteConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRemoteConfigRepository @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
) : RemoteConfigRepository {

    private val _configUpdates = MutableSharedFlow<Unit>(replay = 1)
    override val configUpdates: Flow<Unit> = _configUpdates.asSharedFlow()

    init {
        val fetchIntervalSeconds = if (BuildConfig.DEBUG) 0L else 3600L
        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings { minimumFetchIntervalInSeconds = fetchIntervalSeconds }
        )
        remoteConfig.setDefaultsAsync(
            mapOf(
                KEY_CACHE_TTL_MINUTES to 5L,
                KEY_THEME_TOGGLE_ENABLED to false,
                KEY_LANGUAGE_SELECTION_ENABLED to false,
            )
        )
    }

    override suspend fun fetchAndActivate() {
        runCatching {
            remoteConfig.fetchAndActivate().await()
            _configUpdates.emit(Unit)
        }.onFailure { e ->
            Log.e(TAG, "fetchAndActivate failed: ${e.message}")
        }
    }

    override fun getCacheTtlMinutes(): Int =
        remoteConfig.getLong(KEY_CACHE_TTL_MINUTES).toInt()

    override fun isThemeToggleEnabled(): Boolean =
        remoteConfig.getBoolean(KEY_THEME_TOGGLE_ENABLED)

    override fun isLanguageSelectionEnabled(): Boolean =
        remoteConfig.getBoolean(KEY_LANGUAGE_SELECTION_ENABLED)

    companion object {
        private const val TAG = "RemoteConfig"
        private const val KEY_CACHE_TTL_MINUTES = "cache_ttl_minutes"
        private const val KEY_THEME_TOGGLE_ENABLED = "feature_theme_toggle_enabled"
        private const val KEY_LANGUAGE_SELECTION_ENABLED = "feature_language_selection_enabled"
    }
}
