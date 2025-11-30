package com.example.notesappcompose.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.notesappcompose.Task

class TaskViewModel : ViewModel() {

    // список задач
    val tasks = mutableStateListOf<Task>()

    fun addTask(title: String, details: String?) {
        val t = Task(title = title, details = details)
        tasks.add(t)
    }
}