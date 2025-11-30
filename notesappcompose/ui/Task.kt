package com.example.notesappcompose

data class Task(
        val id: String = java.util.UUID.randomUUID().toString(),
val title: String,
val details: String? = null
        )
