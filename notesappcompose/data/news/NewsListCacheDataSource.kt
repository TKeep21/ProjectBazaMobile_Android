package com.example.notesappcompose.data.news

import android.content.Context
import com.example.notesappcompose.data.NewsArticle
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class NewsListCacheSnapshot(
    val articles: List<NewsArticle>,
    val cachedAtMs: Long,
)

class NewsListCacheDataSource(
    context: Context,
    private val gson: Gson = Gson(),
) {
    private val cacheDir = File(context.filesDir, "news_cache")
    private val cacheFile = File(cacheDir, "news_list.json")

    suspend fun readSnapshot(): NewsListCacheSnapshot? = withContext(Dispatchers.IO) {
        if (cacheFile.exists().not()) return@withContext null

        val payload = runCatching {
            gson.fromJson(cacheFile.readText(), CachedNewsPayload::class.java)
        }.getOrNull()

        if (payload == null) {
            cacheFile.delete()
            return@withContext null
        }

        val mappedArticles = payload.items.orEmpty().mapNotNull { it.toDomain() }
        if (mappedArticles.isEmpty()) return@withContext null

        NewsListCacheSnapshot(
            articles = mappedArticles,
            cachedAtMs = payload.cachedAtMs,
        )
    }

    suspend fun writeSnapshot(articles: List<NewsArticle>, cachedAtMs: Long): Unit = withContext(Dispatchers.IO) {
        if (articles.isEmpty()) return@withContext

        cacheDir.mkdirs()
        val payload = CachedNewsPayload(
            cachedAtMs = cachedAtMs,
            items = articles.map { CachedNewsItem.fromDomain(it) },
        )
        val tmp = File(cacheDir, "news_list.tmp")
        tmp.writeText(gson.toJson(payload))
        if (tmp.renameTo(cacheFile).not()) {
            tmp.copyTo(cacheFile, overwrite = true)
            tmp.delete()
        }
    }

    suspend fun cleanupExpired(retentionMs: Long, nowMs: Long): Unit = withContext(Dispatchers.IO) {
        val snapshot = readSnapshot() ?: return@withContext
        if (nowMs - snapshot.cachedAtMs > retentionMs) {
            cacheFile.delete()
        }
    }

    suspend fun clearAll(): Unit = withContext(Dispatchers.IO) {
        cacheFile.delete()
    }
}

private data class CachedNewsPayload(
    val cachedAtMs: Long,
    val items: List<CachedNewsItem>?,
)

private data class CachedNewsItem(
    val title: String?,
    val abstractText: String?,
    val sourceLabel: String?,
    val publishedAt: String?,
    val imageUrl: String?,
) {
    fun toDomain(): NewsArticle? {
        val safeTitle = title?.trim().takeUnless { it.isNullOrEmpty() } ?: return null
        return NewsArticle(
            title = safeTitle,
            abstractText = abstractText.orEmpty(),
            sourceLabel = sourceLabel.orEmpty().ifEmpty { "The New York Times" },
            publishedAt = publishedAt.orEmpty(),
            imageUrl = imageUrl,
        )
    }

    companion object {
        fun fromDomain(article: NewsArticle): CachedNewsItem {
            return CachedNewsItem(
                title = article.title,
                abstractText = article.abstractText,
                sourceLabel = article.sourceLabel,
                publishedAt = article.publishedAt,
                imageUrl = article.imageUrl,
            )
        }
    }
}
