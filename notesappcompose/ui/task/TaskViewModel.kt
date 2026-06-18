package com.example.notesappcompose.ui.task

import androidx.lifecycle.ViewModel
import com.example.notesappcompose.data.tasks.Priority
import com.example.notesappcompose.data.tasks.Task
import com.example.notesappcompose.data.tasks.TaskRepository
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

class TaskViewModel : ViewModel() {

    private val repo = TaskRepository()

    val tasks: StateFlow<List<Task>> = repo.tasks

    fun addTask(
        title: String,
        description: String,
        priority: Priority,
        flagged: Boolean,
        deadline: LocalDateTime?
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