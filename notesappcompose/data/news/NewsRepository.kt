package com.example.notesappcompose.data.news

import android.content.Context
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class NewsSource {
    NETWORK,
    CACHE,
}

data class NewsLoadPayload(
    val articles: List<NewsArticle>,
    val source: NewsSource,
    val loadedAtMs: Long,
    val warningMessage: String? = null,
)

class NewsRepository(
    private val remoteDataSource: NewsRemoteDataSource,
    private val listCacheDataSource: NewsListCacheDataSource,
) {
    private val mutex = Mutex()

    suspend fun refreshHomeNews(): Result<NewsLoadPayload> = mutex.withLock {
        val now = System.currentTimeMillis()
        listCacheDataSource.cleanupExpired(retentionMs = LIST_CACHE_RETENTION_MS, nowMs = now)

        val networkResult = remoteDataSource.fetchHomeNews()
        val networkArticles = networkResult.getOrNull()
        if (networkArticles != null) {
            listCacheDataSource.writeSnapshot(articles = networkArticles, cachedAtMs = now)
            return Result.success(
                NewsLoadPayload(
                    articles = networkArticles,
                    source = NewsSource.NETWORK,
                    loadedAtMs = now,
                ),
            )
        }

        val cachedSnapshot = listCacheDataSource.readSnapshot()
        if (cachedSnapshot != null) {
            return Result.success(
                NewsLoadPayload(
                    articles = cachedSnapshot.articles,
                    source = NewsSource.CACHE,
                    loadedAtMs = cachedSnapshot.cachedAtMs,
                    warningMessage = "Сеть недоступна, показан сохраненный кэш новостей",
                ),
            )
        }

        return networkResult.fold(
            onSuccess = {
                Result.success(
                    NewsLoadPayload(
                        articles = it,
                        source = NewsSource.NETWORK,
                        loadedAtMs = now,
                    ),
                )
            },
            onFailure = { Result.failure(it) },
        )
    }

    suspend fun loadCachedNewsForFastStart(): NewsLoadPayload? {
        val now = System.currentTimeMillis()
        listCacheDataSource.cleanupExpired(retentionMs = LIST_CACHE_RETENTION_MS, nowMs = now)
        val snapshot = listCacheDataSource.readSnapshot() ?: return null
        return NewsLoadPayload(
            articles = snapshot.articles,
            source = NewsSource.CACHE,
            loadedAtMs = snapshot.cachedAtMs,
            warningMessage = "Показан сохраненный кэш. Выполняется обновление данных...",
        )
    }

    companion object {
        private const val LIST_CACHE_RETENTION_MS = 24L * 60L * 60L * 1000L

        fun create(context: Context): NewsRepository {
            return NewsRepository(
                remoteDataSource = NewsRemoteDataSource.create(),
                listCacheDataSource = NewsListCacheDataSource(context.applicationContext),
            )
        }
    }
}
