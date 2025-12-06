package com.example.notesappcompose.ui

import androidx.lifecycle.ViewModel
import com.example.notesappcompose.data.TaskRepository
import com.example.notesappcompose.data.Priority
import com.example.notesappcompose.data.Task
import kotlinx.coroutines.flow.StateFlow

class TaskViewModel : ViewModel() {

    private val repo = TaskRepository()

    val tasks: StateFlow<List<Task>> = repo.tasks

    fun addTask(
        title: String,
        description: String,
        priority: Priority,
        flagged: Boolean,
        deadline: java.time.LocalDateTime?
    ) {
        val task = Task(
            title = title,
            description = description,
            priority = priority,
            flagged = flagged,
            deadline = deadline
        )
        repo.addTask(task)
    }

    fun toggleComplete(id: Long) {
        repo.toggleComplete(id)
    }
}
