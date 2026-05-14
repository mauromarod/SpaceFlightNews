package com.mauromarod.spaceflightnews.features.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.mauromarod.spaceflightnews.core.designsystem.spacing
import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.core.uicomponents.ErrorState
import com.mauromarod.spaceflightnews.core.uicomponents.ShimmerBox
import com.mauromarod.spaceflightnews.core.uicomponents.NetworkImage
import com.mauromarod.spaceflightnews.features.detail.R
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val noBrowserMsg = stringResource(R.string.error_no_browser)

    DetailEffectHandler(
        viewModel = viewModel,
        lifecycle = lifecycle,
        snackbarHostState = snackbarHostState,
        noBrowserMsg = noBrowserMsg,
        context = context,
    )

    Scaffold(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { DetailTopBar(onBack = onBack) },
    ) { innerPadding ->
        when (val state = uiState) {
            is DetailUiState.Loading -> DetailLoadingState(modifier = Modifier.padding(innerPadding))
            is DetailUiState.Error -> ErrorState(
                message = if (state.isNotFound) stringResource(R.string.error_article_not_found)
                          else stringResource(state.messageRes),
                onRetry = { viewModel.onEvent(DetailUiEvent.RetryClicked) },
                modifier = Modifier.padding(innerPadding),
            )
            is DetailUiState.Content -> ArticleDetail(
                article = state.article,
                onOpenUrl = { viewModel.onEvent(DetailUiEvent.OpenUrlClicked) },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun DetailEffectHandler(
    viewModel: DetailViewModel,
    lifecycle: androidx.lifecycle.Lifecycle,
    snackbarHostState: SnackbarHostState,
    noBrowserMsg: String,
    context: android.content.Context,
) {
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
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.topbar_title).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag(DetailTags.BACK_BUTTON),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
    )
}

@Composable
private fun DetailLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = MaterialTheme.spacing.large),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
    ) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        )

        Column(
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spacing.medium)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(12.dp)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xSmall))

            ShimmerBox(modifier = Modifier.fillMaxWidth().height(28.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(28.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(28.dp))

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            repeat(5) {
                ShimmerBox(modifier = Modifier.fillMaxWidth().height(16.dp))
            }
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.75f).height(16.dp))
        }
    }
}

@Composable
private fun ArticleDetail(
    article: Article,
    onOpenUrl: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(DetailTags.CONTENT),
    ) {
        item { ArticleDetailImage(article) }
        item { ArticleDetailMeta(article) }
        item { ArticleDetailTitle(article) }
        item { ArticleDetailSummary(article) }
        item { ArticleDetailReadButton(onOpenUrl) }
    }
}

@Composable
private fun ArticleDetailImage(article: Article) {
    NetworkImage(
        url = article.imageUrl,
        contentDescription = article.title,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
    )
}

@Composable
private fun ArticleDetailMeta(article: Article) {
    val formatter = remember {
        DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())
    }
    val formattedDate = remember(article.publishedAt) {
        formatter.format(article.publishedAt.atZone(ZoneId.systemDefault()).toLocalDate())
    }

    Text(
        text = "${article.newsSite.uppercase()} · ${formattedDate.uppercase()}",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(
            horizontal = MaterialTheme.spacing.medium,
            vertical = MaterialTheme.spacing.medium,
        ),
    )
}

@Composable
private fun ArticleDetailTitle(article: Article) {
    Text(
        text = article.title,
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
    )
    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
}

@Composable
private fun ArticleDetailSummary(article: Article) {
    Text(
        text = article.summary,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.87f),
        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
    )
    Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
}

@Composable
private fun ArticleDetailReadButton(onOpenUrl: () -> Unit) {
    Button(
        onClick = onOpenUrl,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium)
            .testTag(DetailTags.READ_BUTTON),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(
            text = stringResource(R.string.action_read_full).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
    Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
}

object DetailTags {
    const val CONTENT = "detail_content"
    const val READ_BUTTON = "detail_read_button"
    const val BACK_BUTTON = "detail_back_button"
}
