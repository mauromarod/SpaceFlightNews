package com.mauromarod.spaceflightnews.features.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauromarod.spaceflightnews.core.domain.model.ArticleNotFoundException
import com.mauromarod.spaceflightnews.core.domain.repository.AnalyticsRepository
import com.mauromarod.spaceflightnews.core.domain.repository.CrashReporter
import com.mauromarod.spaceflightnews.core.domain.usecase.GetArticleDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getArticleDetailUseCase: GetArticleDetailUseCase,
    private val analyticsRepository: AnalyticsRepository,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val articleId: Int = checkNotNull(savedStateHandle[ARTICLE_ID_KEY]) {
        "articleId is required in DetailViewModel"
    }

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<DetailUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<DetailUiEffect> = _uiEffect.receiveAsFlow()

    init {
        loadArticle()
    }

    fun onEvent(event: DetailUiEvent) {
        when (event) {
            is DetailUiEvent.OpenUrlClicked -> {
                val content = _uiState.value as? DetailUiState.Content ?: return
                analyticsRepository.trackExternalUrlOpened(articleId)
                viewModelScope.launch { _uiEffect.send(DetailUiEffect.OpenUrl(content.article.url)) }
            }
            is DetailUiEvent.RetryClicked -> loadArticle()
            is DetailUiEvent.BackClicked ->
                viewModelScope.launch { _uiEffect.send(DetailUiEffect.NavigateBack) }
        }
    }

    private fun loadArticle() {
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            crashReporter.log("Loading article id=$articleId")
            getArticleDetailUseCase(articleId)
                .fold(
                    onSuccess = { article ->
                        _uiState.value = DetailUiState.Content(article)
                    },
                    onFailure = { error ->
                        if (error !is ArticleNotFoundException) {
                            crashReporter.recordNonFatal(
                                error,
                                extras = mapOf("article_id" to articleId.toString())
                            )
                        }
                        _uiState.value = DetailUiState.Error(
                            message = error.message ?: "Unknown error",
                            isNotFound = error is ArticleNotFoundException
                        )
                    }
                )
        }
    }

    companion object {
        const val ARTICLE_ID_KEY = "articleId"
    }
}
