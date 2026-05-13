package com.mauromarod.spaceflightnews.features.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.foundation.layout.aspectRatio
import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.core.designsystem.spacing
import com.mauromarod.spaceflightnews.core.uicomponents.ErrorState
import com.mauromarod.spaceflightnews.core.uicomponents.LoadingState
import com.mauromarod.spaceflightnews.core.uicomponents.LocalAnimatedVisibilityScope
import com.mauromarod.spaceflightnews.core.uicomponents.LocalSharedTransitionScope
import com.mauromarod.spaceflightnews.core.uicomponents.NetworkImage
import com.mauromarod.spaceflightnews.features.detail.R
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val noBrowserMsg = stringResource(R.string.error_no_browser)

    LaunchedEffect(Unit) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    is DetailUiEffect.OpenUrl -> {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(effect.url)))
                        } catch (e: ActivityNotFoundException) {
                            snackbarHostState.showSnackbar(noBrowserMsg)
                        }
                    }
                    is DetailUiEffect.NavigateBack -> onBack()
                    is DetailUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.topbar_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.onEvent(DetailUiEvent.BackClicked) },
                        modifier = Modifier.testTag(DetailTags.BACK_BUTTON),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is DetailUiState.Loading -> LoadingState(modifier = Modifier.padding(innerPadding))
            is DetailUiState.Error -> ErrorState(
                message = if (state.isNotFound) stringResource(R.string.error_article_not_found)
                          else state.message,
                onRetry = { viewModel.onEvent(DetailUiEvent.RetryClicked) },
                modifier = Modifier.padding(innerPadding)
            )
            is DetailUiState.Content -> ArticleDetail(
                article = state.article,
                onOpenUrl = { viewModel.onEvent(DetailUiEvent.OpenUrlClicked) },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun ArticleDetail(
    article: Article,
    onOpenUrl: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember {
        DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())
    }
    val formattedDate = remember(article.publishedAt) {
        formatter.format(article.publishedAt.atZone(ZoneId.systemDefault()).toLocalDate())
    }

    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    LazyColumn(modifier = modifier
        .fillMaxSize()
        .testTag(DetailTags.CONTENT)) {
        item {
            NetworkImage(
                url = article.imageUrl,
                contentDescription = article.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .then(
                        if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                            with(sharedTransitionScope) {
                                Modifier.sharedElement(
                                    rememberSharedContentState(key = "image-${article.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                            }
                        } else Modifier
                    )
            )
        }
        item {
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.medium
                )
            )
        }
        item {
            Text(
                text = "${article.newsSite} · $formattedDate",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        }
        item {
            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        }
        item {
            Button(
                onClick = onOpenUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .testTag(DetailTags.READ_BUTTON),
            ) {
                Text(stringResource(R.string.action_read_full))
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        }
    }
}

object DetailTags {
    const val CONTENT = "detail_content"
    const val READ_BUTTON = "detail_read_button"
    const val BACK_BUTTON = "detail_back_button"
}
