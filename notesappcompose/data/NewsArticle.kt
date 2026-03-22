package com.example.notesappcompose.data

data class NewsArticle(
    val title: String,
    val abstractText: String,
    val sourceLabel: String,
    val publishedAt: String,
    val imageUrl: String?,
)
