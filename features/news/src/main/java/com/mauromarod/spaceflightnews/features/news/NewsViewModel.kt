package com.mauromarod.spaceflightnews.features.news

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.core.domain.repository.AnalyticsRepository
import com.mauromarod.spaceflightnews.core.domain.repository.ArticleRepository
import com.mauromarod.spaceflightnews.core.domain.usecase.GetArticlesUseCase
import com.mauromarod.spaceflightnews.core.domain.usecase.SearchArticlesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getArticlesUseCase: GetArticlesUseCase,
    private val searchArticlesUseCase: SearchArticlesUseCase,
    private val repository: ArticleRepository,
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow(
        savedStateHandle.get<String>(KEY_SEARCH_QUERY) ?: ""
    )
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiEffect = Channel<NewsUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<NewsUiEffect> = _uiEffect.receiveAsFlow()

    private val _lastSyncedAt = MutableStateFlow<Instant?>(null)
    val lastSyncedAt: StateFlow<Instant?> = _lastSyncedAt.asStateFlow()

    val articles: Flow<PagingData<Article>> = _searchQuery
        .debounce(500L)
        .flatMapLatest { query ->
            if (query.isBlank()) getArticlesUseCase() else searchArticlesUseCase(query)
        }
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            _lastSyncedAt.value = repository.getLastSyncedAt()
        }
        _searchQuery
            .debounce(500L)
            .onEach { query ->
                if (query.isNotBlank()) {
                    analyticsRepository.trackSearchPerformed(query.length)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: NewsUiEvent) {
        when (event) {
            is NewsUiEvent.SearchQueryChanged -> {
                _searchQuery.value = event.query
                savedStateHandle[KEY_SEARCH_QUERY] = event.query
            }
            is NewsUiEvent.ArticleTapped -> {
                viewModelScope.launch {
                    val source = if (_searchQuery.value.isNotBlank()) "search" else "feed"
                    if (_searchQuery.value.isNotBlank()) {
                        analyticsRepository.trackSearchConverted(event.articleId)
                    }
                    _uiEffect.send(NewsUiEffect.NavigateToDetail(event.articleId))
                }
            }
            is NewsUiEvent.RetryClicked -> Unit
        }
    }

    fun onArticleImpression(articleId: Int, title: String, newsSite: String) {
        val source = if (_searchQuery.value.isNotBlank()) "search" else "feed"
        analyticsRepository.trackArticleOpened(articleId, title, newsSite, source)
    }

    companion object {
        private const val KEY_SEARCH_QUERY = "search_query"
    }
}
