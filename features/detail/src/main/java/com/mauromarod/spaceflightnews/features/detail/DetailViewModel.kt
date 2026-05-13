package com.mauromarod.spaceflightnews.features.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauromarod.spaceflightnews.core.domain.model.ArticleNotFoundException
import com.mauromarod.spaceflightnews.core.domain.repository.AnalyticsRepository
import com.mauromarod.spaceflightnews.core.domain.repository.CrashReporter
import com.mauromarod.spaceflightnews.core.domain.repository.RemoteConfigRepository
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
    private val savedStateHandle: SavedStateHandle,
    private val getArticleDetailUseCase: GetArticleDetailUseCase,
    private val analyticsRepository: AnalyticsRepository,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<DetailUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<DetailUiEffect> = _uiEffect.receiveAsFlow()

    init {
        savedStateHandle.get<Int>(ARTICLE_ID_KEY)?.let { id ->
            loadArticle(id)
        }
    }

    fun setArticleId(id: Int) {
        if (savedStateHandle.get<Int>(ARTICLE_ID_KEY) == id) return
        savedStateHandle[ARTICLE_ID_KEY] = id
        loadArticle(id)
    }

    fun onEvent(event: DetailUiEvent) {
        when (event) {
            is DetailUiEvent.OpenUrlClicked -> {
                val content = _uiState.value as? DetailUiState.Content ?: return
                val id = savedStateHandle.get<Int>(ARTICLE_ID_KEY) ?: return
                analyticsRepository.trackExternalUrlOpened(id)
                viewModelScope.launch { _uiEffect.send(DetailUiEffect.OpenUrl(content.article.url)) }
            }
            is DetailUiEvent.RetryClicked -> {
                savedStateHandle.get<Int>(ARTICLE_ID_KEY)?.let { id ->
                    loadArticle(id)
                }
            }
        }
    }

    private fun loadArticle(id: Int) {
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            crashReporter.log("Loading article id=$id")
            getArticleDetailUseCase(id)
                .fold(
                    onSuccess = { article ->
                        _uiState.value = DetailUiState.Content(article)
                    },
                    onFailure = { error ->
                        if (error !is ArticleNotFoundException) {
                            crashReporter.recordNonFatal(
                                error,
                                extras = mapOf("article_id" to id.toString())
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

    fun getArticleId(): Int? = savedStateHandle.get<Int>(ARTICLE_ID_KEY)

    companion object {
        const val ARTICLE_ID_KEY = "articleId"
    }
}
