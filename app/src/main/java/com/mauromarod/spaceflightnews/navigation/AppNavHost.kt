package com.mauromarod.spaceflightnews.navigation

import android.content.res.Configuration
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mauromarod.spaceflightnews.features.detail.DetailViewModel
import com.mauromarod.spaceflightnews.core.designsystem.spacing
import com.mauromarod.spaceflightnews.core.domain.repository.AuthRepository
import com.mauromarod.spaceflightnews.core.uicomponents.R as UiR
import com.mauromarod.spaceflightnews.core.uicomponents.LocalAnimatedVisibilityScope
import com.mauromarod.spaceflightnews.core.uicomponents.LocalSharedTransitionScope
import com.mauromarod.spaceflightnews.features.detail.DetailScreen
import com.mauromarod.spaceflightnews.features.news.NewsScreen
import com.mauromarod.spaceflightnews.features.news.NewsViewModel
import com.mauromarod.spaceflightnews.auth.LoginScreen
import com.mauromarod.spaceflightnews.profile.ProfileScreen

@Composable
fun AppNavHost(
    authRepository: AuthRepository,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val isLandscapeTablet = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        && configuration.screenWidthDp >= 600

    val startDestination = if (authRepository.isLoggedIn()) Screen.NewsList.route else Screen.Login.route

    var selectedArticleId by rememberSaveable { mutableStateOf<Int?>(null) }

    if (isLandscapeTablet) {
        TwoPaneLayout(
            startDestination = startDestination,
            selectedArticleId = selectedArticleId,
            onArticleSelected = { selectedArticleId = it },
            onArticleDeselected = { selectedArticleId = null },
            modifier = modifier,
        )
    } else {
        SinglePaneLayout(
            startDestination = startDestination,
            selectedArticleId = selectedArticleId,
            onArticleOpened = { selectedArticleId = it },
            onArticleClosed = { selectedArticleId = null },
            modifier = modifier,
        )
    }
}

@Composable
private fun SinglePaneLayout(
    startDestination: String,
    selectedArticleId: Int?,
    onArticleOpened: (Int) -> Unit,
    onArticleClosed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    // Restore article when switching from landscape two-pane to portrait single-pane.
    LaunchedEffect(selectedArticleId) {
        val id = selectedArticleId
        if (id != null) {
            val currentRoute = navController.currentDestination?.route
            val detailRoute = Screen.ArticleDetail.createRoute(id)
            if (currentRoute != detailRoute) {
                navController.navigate(detailRoute)
            }
        }
    }

    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = modifier.semantics { testTagsAsResourceId = true }
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        onNavigateToNews = {
                            navController.navigate(Screen.NewsList.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.NewsList.route) {
                    val newsViewModel: NewsViewModel = hiltViewModel()
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        NewsScreen(
                            onNavigateToDetail = { articleId ->
                                onArticleOpened(articleId)
                                navController.navigate(Screen.ArticleDetail.createRoute(articleId))
                            },
                            onNavigateToProfile = {
                                navController.navigate(Screen.Profile.route)
                            },
                            viewModel = newsViewModel,
                        )
                    }
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onBack = { navController.popBackStack() },
                        onSignOut = {
                            onArticleClosed()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = Screen.ArticleDetail.route,
                    arguments = listOf(
                        navArgument(Screen.ArticleDetail.ARG_ARTICLE_ID) { type = NavType.IntType }
                    )
                ) {
                    val detailViewModel: DetailViewModel = hiltViewModel()
                    BackHandler {
                        onArticleClosed()
                        navController.popBackStack()
                    }
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        DetailScreen(
                            onBack = {
                                onArticleClosed()
                                navController.popBackStack()
                            },
                            viewModel = detailViewModel,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TwoPaneLayout(
    startDestination: String,
    selectedArticleId: Int?,
    onArticleSelected: (Int) -> Unit,
    onArticleDeselected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mainNavController = rememberNavController()

    NavHost(
        navController = mainNavController,
        startDestination = startDestination,
        modifier = modifier.semantics { testTagsAsResourceId = true },
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToNews = {
                    mainNavController.navigate(Screen.NewsList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.NewsList.route) {
            TwoPaneContent(
                selectedArticleId = selectedArticleId,
                onArticleSelected = onArticleSelected,
                onArticleDeselected = onArticleDeselected,
                onNavigateToProfile = {
                    mainNavController.navigate(Screen.Profile.route)
                },
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack = { mainNavController.popBackStack() },
                onSignOut = {
                    onArticleDeselected()
                    mainNavController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
internal fun TwoPaneContent(
    selectedArticleId: Int?,
    onArticleSelected: (Int) -> Unit,
    onArticleDeselected: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val detailViewModel: DetailViewModel = hiltViewModel()

    BackHandler(enabled = selectedArticleId != null) {
        onArticleDeselected()
    }

    LaunchedEffect(selectedArticleId) {
        selectedArticleId?.let { id ->
            detailViewModel.setArticleId(id)
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true }
    ) {
        val newsViewModel: NewsViewModel = hiltViewModel()
        NewsScreen(
            onNavigateToDetail = { articleId -> onArticleSelected(articleId) },
            onNavigateToProfile = onNavigateToProfile,
            viewModel = newsViewModel,
            modifier = Modifier.weight(1f)
        )

        VerticalDivider()

        if (selectedArticleId != null) {
            DetailScreen(
                onBack = { onArticleDeselected() },
                viewModel = detailViewModel,
                modifier = Modifier.weight(1f)
            )
        } else {
            EmptyDetailPane(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun EmptyDetailPane(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(DETAIL_EMPTY_PANE_TAG),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Article,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            Text(
                text = stringResource(UiR.string.detail_placeholder),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private const val DETAIL_EMPTY_PANE_TAG = "detail_empty_pane"
