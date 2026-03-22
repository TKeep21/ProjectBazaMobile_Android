package com.example.notesappcompose.data.remote

import com.google.gson.annotations.SerializedName

data class TopStoriesResponseDto(
    val status: String?,
    val results: List<NyTimesArticleDto>?,
)

data class NyTimesArticleDto(
    val title: String?,
    val abstract: String?,
    val section: String?,
    @SerializedName("published_date") val publishedDate: String?,
    val multimedia: List<NyTimesMultimediaDto>?,
)

data class NyTimesMultimediaDto(
    val url: String?,
    val format: String?,
)
