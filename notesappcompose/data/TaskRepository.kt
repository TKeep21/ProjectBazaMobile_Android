package com.example.notesappcompose.data


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TaskRepository {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    fun addTask(task: Task) {
        _tasks.value = _tasks.value + task
    }

    fun toggleComplete(id: Long) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == id) task.copy(completed = !task.completed) else task
        }
    }

    fun sortedByPriority(): List<Task> {
        return _tasks.value.sortedByDescending { it.priority }
    }
}