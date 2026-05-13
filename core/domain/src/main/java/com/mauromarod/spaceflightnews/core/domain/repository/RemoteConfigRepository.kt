package com.mauromarod.spaceflightnews.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface RemoteConfigRepository {
    /** Emits Unit after each successful fetchAndActivate so observers can re-read flags. */
    val configUpdates: Flow<Unit>
    suspend fun fetchAndActivate()
    fun getCacheTtlMinutes(): Int
    fun isThemeToggleEnabled(): Boolean
    fun isLanguageSelectionEnabled(): Boolean
}
