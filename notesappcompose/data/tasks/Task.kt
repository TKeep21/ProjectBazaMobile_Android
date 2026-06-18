package com.example.notesappcompose.data.tasks

import java.time.LocalDateTime

data class Task(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val flagged: Boolean = false,
    val deadline: LocalDateTime? = null,
    val completed: Boolean = false
)
