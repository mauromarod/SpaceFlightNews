package com.mauromarod.spaceflightnews.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mauromarod.spaceflightnews.R
import com.mauromarod.spaceflightnews.core.designsystem.SpaceBlue
import com.mauromarod.spaceflightnews.core.designsystem.SpaceDeepNavy
import com.mauromarod.spaceflightnews.core.designsystem.spacing
import com.mauromarod.spaceflightnews.core.domain.model.ThemePreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ProfileUiEffect.NavigateToLogin -> onSignOut()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.profile_cd_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = MaterialTheme.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(MaterialTheme.spacing.xLarge))

            UserAvatar(
                isAnonymous = uiState.user?.isAnonymous ?: true,
                email = uiState.user?.email,
            )

            Spacer(Modifier.height(MaterialTheme.spacing.large))

            HorizontalDivider()

            if (uiState.isThemeEnabled) {
                Spacer(Modifier.height(MaterialTheme.spacing.medium))
                SettingSection(title = stringResource(R.string.profile_section_theme)) {
                    ThemeChips(
                        selected = uiState.theme,
                        onSelect = viewModel::setTheme,
                    )
                }
                Spacer(Modifier.height(MaterialTheme.spacing.medium))
                HorizontalDivider()
            }

            Spacer(Modifier.height(MaterialTheme.spacing.xLarge))

            OutlinedButton(
                onClick = viewModel::signOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(ProfileTags.SIGN_OUT_BUTTON),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.padding(end = MaterialTheme.spacing.small),
                )
                Text(stringResource(R.string.profile_sign_out))
            }

            Spacer(Modifier.height(MaterialTheme.spacing.large))
        }
    }
}

@Composable
private fun UserAvatar(
    isAnonymous: Boolean,
    email: String?,
) {
    val label = if (isAnonymous) {
        stringResource(R.string.profile_guest_name)
    } else {
        email ?: stringResource(R.string.profile_guest_name)
    }

    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(SpaceDeepNavy),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.RocketLaunch,
            contentDescription = stringResource(R.string.profile_cd_avatar),
            tint = SpaceBlue,
            modifier = Modifier.size(48.dp),
        )
    }

    Spacer(Modifier.height(MaterialTheme.spacing.medium))

    Text(
        text = label,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
    )

    if (!isAnonymous && email != null) {
        Spacer(Modifier.height(MaterialTheme.spacing.xSmall))
        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun ThemeChips(
    selected: ThemePreference,
    onSelect: (ThemePreference) -> Unit,
) {
    val options = listOf(
        ThemePreference.SYSTEM to stringResource(R.string.profile_theme_system),
        ThemePreference.LIGHT to stringResource(R.string.profile_theme_light),
        ThemePreference.DARK to stringResource(R.string.profile_theme_dark),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
        options.forEach { (pref, label) ->
            FilterChip(
                selected = selected == pref,
                onClick = { onSelect(pref) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SpaceBlue,
                    selectedLabelColor = SpaceDeepNavy,
                ),
                modifier = Modifier.testTag(
                    when (pref) {
                        ThemePreference.SYSTEM -> ProfileTags.THEME_SYSTEM
                        ThemePreference.LIGHT -> ProfileTags.THEME_LIGHT
                        ThemePreference.DARK -> ProfileTags.THEME_DARK
                    }
                ),
            )
        }
    }
}

object ProfileTags {
    const val SIGN_OUT_BUTTON = "profile_sign_out_button"
    const val THEME_SYSTEM = "profile_theme_system"
    const val THEME_LIGHT = "profile_theme_light"
    const val THEME_DARK = "profile_theme_dark"
}
