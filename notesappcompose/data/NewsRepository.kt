package com.example.notesappcompose.data

import com.example.notesappcompose.BuildConfig
import com.example.notesappcompose.data.remote.NyTimesArticleDto
import com.example.notesappcompose.data.remote.NyTimesMultimediaDto
import com.example.notesappcompose.network.NyTimesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NewsRepository(
    private val api: NyTimesApi,
    private val apiKey: String,
) {
    private val mutex = Mutex()

    suspend fun loadHomeNews(): Result<List<NewsArticle>> = mutex.withLock {
        withContext(Dispatchers.IO) {
            val key = apiKey.trim()
            if (key.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException(
                        "Укажите ключ API: в local.properties добавьте строку nyt.api.key=... (регистрация на https://developer.nytimes.com)",
                    ),
                )
            }
            runCatching {
                val response = api.getHomeTopStories(key)
                response.results.orEmpty().mapNotNull { it.toDomain() }
            }
        }
    }

    companion object {
        fun create(): NewsRepository {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.nytimes.com/svc/topstories/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return NewsRepository(
                api = retrofit.create(NyTimesApi::class.java),
                apiKey = BuildConfig.NYT_API_KEY,
            )
        }
    }
}

private fun NyTimesArticleDto.toDomain(): NewsArticle? {
    val safeTitle = title?.trim().takeUnless { it.isNullOrEmpty() } ?: return null
    return NewsArticle(
        title = safeTitle,
        abstractText = abstract.orEmpty(),
        sourceLabel = section?.trim().orEmpty().ifEmpty { "The New York Times" },
        publishedAt = publishedDate.orEmpty(),
        imageUrl = multimedia.pickPreviewUrl(),
    )
}

private fun List<NyTimesMultimediaDto>?.pickPreviewUrl(): String? {
    val pairs = this.orEmpty().mapNotNull { dto ->
        val u = dto.url?.trim() ?: return@mapNotNull null
        if (!u.startsWith("http")) return@mapNotNull null
        dto to u
    }
    if (pairs.isEmpty()) return null
    val preferred = pairs.firstOrNull { (dto, _) ->
        val fmt = dto.format.orEmpty()
        fmt.contains("thumb", ignoreCase = true) ||
            fmt.contains("ThreeByTwo", ignoreCase = true) ||
            fmt.contains("Large", ignoreCase = true)
    }?.second
    return preferred ?: pairs.first().second
}
