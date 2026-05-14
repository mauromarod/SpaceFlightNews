package com.mauromarod.spaceflightnews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.mauromarod.spaceflightnews.config.RemoteConfigLifecycleObserver
import com.mauromarod.spaceflightnews.core.designsystem.SpaceFlightNewsTheme
import com.mauromarod.spaceflightnews.core.domain.model.ThemePreference
import com.mauromarod.spaceflightnews.core.domain.repository.AuthRepository
import com.mauromarod.spaceflightnews.core.domain.repository.RemoteConfigRepository
import com.mauromarod.spaceflightnews.core.domain.repository.UserPreferencesRepository
import com.mauromarod.spaceflightnews.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository
    @Inject lateinit var remoteConfigRepository: RemoteConfigRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        lifecycle.addObserver(
            RemoteConfigLifecycleObserver(remoteConfigRepository, lifecycleScope)
        )

        val currentUserFlow = authRepository.currentUser
            .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(5_000), null)

        val themeFlow = currentUserFlow
            .filterNotNull()
            .flatMapLatest { user -> userPreferencesRepository.getThemePreference(user.uid) }
            .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(5_000), ThemePreference.SYSTEM)

        setContent {
            val systemDark = isSystemInDarkTheme()
            val themePref by themeFlow.collectAsState()

            val isDarkTheme = when (themePref) {
                ThemePreference.DARK -> true
                ThemePreference.LIGHT -> false
                ThemePreference.SYSTEM -> systemDark
            }

            LaunchedEffect(Unit) {
                reportFullyDrawn()
            }

            SpaceFlightNewsTheme(darkTheme = isDarkTheme) {
                AppNavHost(authRepository = authRepository)
            }
        }
    }
}
