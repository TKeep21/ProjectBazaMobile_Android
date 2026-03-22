package com.example.notesappcompose.data

import java.time.LocalDateTime

data class Note(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val content: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

