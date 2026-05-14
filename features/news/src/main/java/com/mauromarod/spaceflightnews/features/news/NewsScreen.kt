package com.mauromarod.spaceflightnews.features.news

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.mauromarod.spaceflightnews.core.common.ext.toFriendlyMessage
import com.mauromarod.spaceflightnews.core.uicomponents.NetworkImage
import com.mauromarod.spaceflightnews.features.news.util.buildOfflineMessage
import java.io.IOException
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: NewsViewModel,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val articles = viewModel.articles.collectAsLazyPagingItems()
    val lastSyncedAt by viewModel.lastSyncedAt.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        snapshotFlow {
            val refresh = articles.loadState.mediator?.refresh
            refresh is LoadState.Error && articles.itemCount > 0
        }.collect { showError ->
            if (showError) {
                snackbarHostState.showSnackbar(
                    message = buildOfflineMessage(context, lastSyncedAt),
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }

    Scaffold(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            NewsHeader(
                searchQuery = searchQuery,
                onQueryChange = { viewModel.onEvent(NewsUiEvent.SearchQueryChanged(it)) },
                onClearQuery = { viewModel.onEvent(NewsUiEvent.SearchQueryChanged("")) },
                onSearch = { viewModel.onEvent(NewsUiEvent.SearchQueryChanged(it)) },
                onNavigateToProfile = onNavigateToProfile,
            )

            NewsContent(
                articles = articles,
                onArticleTapped = { articleId ->
                    viewModel.onEvent(NewsUiEvent.ArticleTapped(articleId))
                    onNavigateToDetail(articleId)
                },
                onRetry = { articles.refresh() },
            )
        }
    }
}

@Composable
private fun NewsHeader(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onSearch: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = MaterialTheme.spacing.medium),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "SPACE FLIGHT NEWS",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            IconButton(onClick = onNavigateToProfile) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = stringResource(R.string.cd_open_profile),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        ArticleSearchBar(
            query = searchQuery,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            onClearQuery = onClearQuery,
            placeholder = stringResource(R.string.search_placeholder),
            modifier = Modifier.padding(top = MaterialTheme.spacing.small, bottom = MaterialTheme.spacing.medium),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsContent(
    articles: LazyPagingItems<Article>,
    onArticleTapped: (Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
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
                    modifier = modifier,
                )
            } else {
                ErrorState(
                    message = mediatorRefresh.error.toFriendlyMessage(),
                    onRetry = onRetry,
                    modifier = modifier,
                )
            }

        sourceRefresh is LoadState.Error && mediatorRefresh == null ->
            ErrorState(
                message = sourceRefresh.error.toFriendlyMessage(),
                onRetry = onRetry,
                modifier = modifier,
            )

        articles.itemCount == 0
            && mediatorRefresh is LoadState.NotLoading
            && sourceRefresh is LoadState.NotLoading ->
            EmptyState(
                title = stringResource(R.string.empty_no_articles_title),
                subtitle = stringResource(R.string.empty_no_articles_subtitle),
                modifier = modifier,
            )

        else -> PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRetry,
            state = pullState,
            modifier = modifier,
        ) {
            ArticleList(articles = articles, onArticleTapped = onArticleTapped)
        }
    }
}

@Composable
private fun ArticleList(
    articles: LazyPagingItems<Article>,
    onArticleTapped: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = remember {
        DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    }
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    LazyVerticalStaggeredGrid(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(NewsTags.ARTICLE_LIST),
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(MaterialTheme.spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        verticalItemSpacing = MaterialTheme.spacing.small,

    ) {
        items(
            count = articles.itemCount,
            key = articles.itemKey { it.id },
        ) { index ->
            val article = articles[index] ?: return@items

            ArticleCard(
                onClick = { onArticleTapped(article.id) },
                image = {
                    NetworkImage(
                        url = article.imageUrl,
                        contentScale = ContentScale.FillWidth,
                        contentDescription = article.title,
                        modifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                            with(sharedTransitionScope) {
                                Modifier.sharedElement(
                                    rememberSharedContentState(key = "image-${article.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                )
                            }
                        } else Modifier,
                    )
                },
                headline = {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = MaterialTheme.spacing.xSmall),
                    )
                },
                supporting = {
                    Text(
                        text = "${article.newsSite.uppercase()} · ${
                            formatter.format(article.publishedAt.atZone(ZoneId.systemDefault()).toLocalDate()).uppercase()
                        }",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }

        if (articles.loadState.append is LoadState.Loading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(MaterialTheme.spacing.medium),
                color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }
    }
}

object NewsTags {
    const val ARTICLE_LIST = "news_article_list"
}
