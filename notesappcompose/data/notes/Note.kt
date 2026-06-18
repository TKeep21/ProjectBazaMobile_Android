package com.example.notesappcompose.data.notes

import java.time.LocalDateTime

data class Note(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val content: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
