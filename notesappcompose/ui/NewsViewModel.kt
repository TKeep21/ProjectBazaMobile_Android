package com.example.notesappcompose.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesappcompose.data.NewsArticle
import com.example.notesappcompose.data.NewsRepository
import com.example.notesappcompose.network.DebugNetworkClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private fun Throwable.toNewsLoadMessage(): String {
    var current: Throwable? = this
    while (current != null) {
        when (current) {
            is UnknownHostException -> {
                return "Нет связи с сервером (не находится адрес api.nytimes.com). " +
                    "Проверьте интернет на устройстве или эмуляторе (в браузере откройте любой сайт). " +
                    "На эмуляторе иногда помогает Cold Boot или смена сети. " +
                    "Если в вашем регионе недоступен NYT — попробуйте VPN."
            }
            is SocketTimeoutException -> {
                return "Таймаут сети. Проверьте подключение и попробуйте снова."
            }
        }
        current = current.cause
    }
    val raw = message
    return if (raw.isNullOrBlank()) "Не удалось загрузить новости" else raw
}

data class NewsUiState(
    val isLoading: Boolean = true,
    val refreshInProgress: Boolean = false,
    val articles: List<NewsArticle> = emptyList(),
    val errorMessage: String? = null,
)

class NewsViewModel(
    private val repository: NewsRepository = NewsRepository.create(),
) : ViewModel() {

    private val _state = MutableStateFlow(NewsUiState())
    val state: StateFlow<NewsUiState> = _state.asStateFlow()

    private var pollJob: Job? = null

    init {
        viewModelScope.launch {
            DebugNetworkClient.logSamplePostWithJsonBody()
        }
        startPeriodicRefresh()
    }

    private fun startPeriodicRefresh() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                refreshInternal()
                delay(120_000L)
            }
        }
    }

    fun retryAfterError() {
        viewModelScope.launch {
            refreshInternal()
        }
    }

    private suspend fun refreshInternal() {
        val hadContent = _state.value.articles.isNotEmpty()
        _state.update { current ->
            when {
                hadContent -> current.copy(refreshInProgress = true)
                else -> current.copy(isLoading = true, errorMessage = null)
            }
        }
        val result = repository.loadHomeNews()
        val list = result.getOrNull()
        if (list != null) {
            _state.update {
                it.copy(
                    isLoading = false,
                    refreshInProgress = false,
                    articles = list,
                    errorMessage = null,
                )
            }
        } else {
            val err = result.exceptionOrNull()
            _state.update {
                it.copy(
                    isLoading = false,
                    refreshInProgress = false,
                    errorMessage = err?.toNewsLoadMessage() ?: "Не удалось загрузить новости",
                )
            }
        }
    }

    override fun onCleared() {
        pollJob?.cancel()
        super.onCleared()
    }
}
