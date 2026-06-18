package com.example.notesappcompose.ui.task

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val details: String? = null
        )