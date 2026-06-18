package com.example.notesappcompose.ui.news

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesappcompose.data.news.NewsLoadPayload
import com.example.notesappcompose.data.news.NewsRepository
import com.example.notesappcompose.data.news.NewsSource
import com.example.notesappcompose.data.news.NewsArticle
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
import java.text.DateFormat
import java.util.Date
import java.util.Locale

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
    val infoMessage: String? = null,
    val sourceLabel: String? = null,
    val lastUpdatedLabel: String? = null,
)

class NewsViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val repository: NewsRepository = NewsRepository.create(application)

    private val _state = MutableStateFlow(NewsUiState())
    val state: StateFlow<NewsUiState> = _state.asStateFlow()

    private var pollJob: Job? = null

    init {
        viewModelScope.launch {
            showCachedForFastStart()
            startPeriodicRefresh()
        }
        viewModelScope.launch {
            DebugNetworkClient.logSamplePostWithJsonBody()
        }
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

    private suspend fun showCachedForFastStart() {
        val cached = repository.loadCachedNewsForFastStart() ?: return
        applyPayload(
            payload = cached,
            loading = false,
            refreshing = false,
        )
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
                hadContent -> current.copy(refreshInProgress = true, errorMessage = null)
                else -> current.copy(isLoading = true, errorMessage = null)
            }
        }

        val result = repository.refreshHomeNews()
        val payload = result.getOrNull()
        if (payload != null) {
            applyPayload(
                payload = payload,
                loading = false,
                refreshing = false,
            )
            return
        }

        val error = result.exceptionOrNull()?.toNewsLoadMessage() ?: "Не удалось загрузить новости"
        _state.update {
            it.copy(
                isLoading = false,
                refreshInProgress = false,
                errorMessage = error,
                infoMessage = null,
            )
        }
    }

    private fun applyPayload(payload: NewsLoadPayload, loading: Boolean, refreshing: Boolean) {
        _state.update {
            it.copy(
                isLoading = loading,
                refreshInProgress = refreshing,
                articles = payload.articles,
                errorMessage = null,
                infoMessage = payload.warningMessage,
                sourceLabel = payload.source.toUiLabel(),
                lastUpdatedLabel = payload.loadedAtMs.toUiDateTime(),
            )
        }
    }

    override fun onCleared() {
        pollJob?.cancel()
        super.onCleared()
    }
}

private fun NewsSource.toUiLabel(): String {
    return when (this) {
        NewsSource.NETWORK -> "Источник: сеть"
        NewsSource.CACHE -> "Источник: кэш"
    }
}

private fun Long.toUiDateTime(): String {
    val format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
    return format.format(Date(this))
}
