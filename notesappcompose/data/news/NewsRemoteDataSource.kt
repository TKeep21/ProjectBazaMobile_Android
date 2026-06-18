package com.example.notesappcompose.data.news

import com.example.notesappcompose.BuildConfig
import com.example.notesappcompose.data.news.NewsArticle
import com.example.notesappcompose.data.remote.NyTimesArticleDto
import com.example.notesappcompose.data.remote.NyTimesMultimediaDto
import com.example.notesappcompose.network.NyTimesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NewsRemoteDataSource(
    private val api: NyTimesApi,
    private val apiKey: String,
) {
    suspend fun fetchHomeNews(): Result<List<NewsArticle>> = withContext(Dispatchers.IO) {
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

    companion object {
        fun create(): NewsRemoteDataSource {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.nytimes.com/svc/topstories/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return NewsRemoteDataSource(
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
        val url = dto.url?.trim() ?: return@mapNotNull null
        if (url.startsWith("http").not()) return@mapNotNull null
        dto to url
    }
    if (pairs.isEmpty()) return null

    val preferredUrl = pairs.firstOrNull { (dto, _) ->
        val format = dto.format.orEmpty()
        format.contains("thumb", ignoreCase = true) ||
            format.contains("ThreeByTwo", ignoreCase = true) ||
            format.contains("Large", ignoreCase = true)
    }?.second

    return preferredUrl ?: pairs.first().second
}
