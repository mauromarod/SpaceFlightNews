package com.mauromarod.spaceflightnews.features.news

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.mauromarod.spaceflightnews.core.designsystem.spacing
import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.core.uicomponents.ArticleCard
import com.mauromarod.spaceflightnews.core.uicomponents.ArticleSearchBar
import com.mauromarod.spaceflightnews.core.uicomponents.EmptyState
import com.mauromarod.spaceflightnews.core.uicomponents.ErrorState
import com.mauromarod.spaceflightnews.core.uicomponents.LoadingState
import com.mauromarod.spaceflightnews.core.uicomponents.LocalAnimatedVisibilityScope
import com.mauromarod.spaceflightnews.core.uicomponents.LocalSharedTransitionScope
import com.mauromarod.spaceflightnews.core.uicomponents.NetworkImage
import com.mauromarod.spaceflightnews.features.news.R
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import androidx.lifecycle.compose.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val articles = viewModel.articles.collectAsLazyPagingItems()
    val lastSyncedAt by viewModel.lastSyncedAt.collectAsState()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    is NewsUiEffect.NavigateToDetail -> onNavigateToDetail(effect.articleId)
                    is NewsUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    LaunchedEffect(articles.loadState.mediator?.refresh) {
        val refresh = articles.loadState.mediator?.refresh
        if (refresh is LoadState.Error && articles.itemCount > 0) {
            snackbarHostState.showSnackbar(
                message = buildOfflineMessage(context, lastSyncedAt),
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.small
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArticleSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onEvent(NewsUiEvent.SearchQueryChanged(it)) },
                    onSearch = { viewModel.onEvent(NewsUiEvent.SearchQueryChanged(it)) },
                    onClearQuery = { viewModel.onEvent(NewsUiEvent.SearchQueryChanged("")) },
                    placeholder = stringResource(R.string.search_placeholder),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onNavigateToProfile) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = stringResource(R.string.cd_open_profile),
                    )
                }
            }

            NewsContent(
                articles = articles,
                onArticleTapped = { viewModel.onEvent(NewsUiEvent.ArticleTapped(it)) },
                onRetry = { articles.refresh() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsContent(
    articles: LazyPagingItems<Article>,
    onArticleTapped: (Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mediatorRefresh = articles.loadState.mediator?.refresh
    val sourceRefresh = articles.loadState.source.refresh
    val isRefreshing = mediatorRefresh is LoadState.Loading
    val pullState = rememberPullToRefreshState()

    when {
        (mediatorRefresh == null
            || mediatorRefresh is LoadState.Loading
            || sourceRefresh is LoadState.Loading) && articles.itemCount == 0 ->
            LoadingState(modifier = modifier)

        mediatorRefresh is LoadState.Error && articles.itemCount == 0 ->
            if (mediatorRefresh.error is IOException) {
                EmptyState(
                    title = stringResource(R.string.empty_no_results_title),
                    subtitle = stringResource(R.string.empty_no_results_subtitle),
                    modifier = modifier
                )
            } else {
                ErrorState(
                    message = mediatorRefresh.error.toFriendlyMessage(),
                    onRetry = onRetry,
                    modifier = modifier
                )
            }

        sourceRefresh is LoadState.Error && mediatorRefresh == null ->
            ErrorState(
                message = sourceRefresh.error.toFriendlyMessage(),
                onRetry = onRetry,
                modifier = modifier
            )

        articles.itemCount == 0
            && mediatorRefresh is LoadState.NotLoading
            && sourceRefresh is LoadState.NotLoading ->
            EmptyState(
                title = stringResource(R.string.empty_no_articles_title),
                subtitle = stringResource(R.string.empty_no_articles_subtitle),
                modifier = modifier
            )

        else -> PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRetry,
            state = pullState,
            modifier = modifier
        ) {
            ArticleList(
                articles = articles,
                onArticleTapped = onArticleTapped
            )
        }
    }
}

@Composable
private fun ArticleList(
    articles: LazyPagingItems<Article>,
    onArticleTapped: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag(NewsTags.ARTICLE_LIST),
        contentPadding = PaddingValues(MaterialTheme.spacing.small)
    ) {
        items(
            count = articles.itemCount,
            key = articles.itemKey { it.id }
        ) { index ->
            val article = articles[index] ?: return@items
            val sharedTransitionScope = LocalSharedTransitionScope.current
            val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
            ArticleCard(
                onClick = { onArticleTapped(article.id) },
                modifier = Modifier.padding(vertical = MaterialTheme.spacing.xSmall),
                image = {
                    NetworkImage(
                        url = article.imageUrl,
                        contentDescription = article.title,
                        modifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                            with(sharedTransitionScope) {
                                Modifier.sharedElement(
                                    rememberSharedContentState(key = "image-${article.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                            }
                        } else Modifier
                    )
                },
                headline = {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2
                    )
                },
                supporting = {
                    Text(
                        text = "${article.newsSite} · ${formatter.format(
                            article.publishedAt.atZone(ZoneId.systemDefault()).toLocalDate()
                        )}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }

        if (articles.loadState.append is LoadState.Loading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(MaterialTheme.spacing.medium)
                    )
                }
            }
        }
    }
}

private fun buildOfflineMessage(context: android.content.Context, lastSyncedAt: Instant?): String {
    if (lastSyncedAt == null) return context.getString(R.string.offline_cached)
    val minutesAgo = ChronoUnit.MINUTES.between(lastSyncedAt, Instant.now())
    return when {
        minutesAgo < 1  -> context.getString(R.string.offline_just_now)
        minutesAgo < 60 -> context.getString(R.string.offline_minutes_ago, minutesAgo.toInt())
        else            -> context.getString(R.string.offline_hours_ago, ChronoUnit.HOURS.between(lastSyncedAt, Instant.now()).toInt())
    }
}

private fun Throwable.toFriendlyMessage(): String =
    if (this is IOException) "No internet connection" else message ?: "Something went wrong"

object NewsTags {
    const val ARTICLE_LIST = "news_article_list"
}
