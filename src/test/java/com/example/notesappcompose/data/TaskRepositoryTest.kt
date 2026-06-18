package com.example.notesappcompose.data

import com.example.notesappcompose.data.tasks.Priority
import com.example.notesappcompose.data.tasks.Task
import com.example.notesappcompose.data.tasks.TaskRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskRepositoryTest {
    @Test
    fun addTaskAppendsTaskToState() {
        val repository = TaskRepository()
        val task = task(id = 1L, title = "Read news")

        repository.addTask(task)

        assertEquals(listOf(task), repository.tasks.value)
    }

    @Test
    fun toggleCompleteChangesOnlyMatchingTask() {
        val repository = TaskRepository()
        repository.addTask(task(id = 1L, title = "First"))
        repository.addTask(task(id = 2L, title = "Second"))

        repository.toggleComplete(2L)

        assertFalse(repository.tasks.value.first { it.id == 1L }.completed)
        assertTrue(repository.tasks.value.first { it.id == 2L }.completed)
    }

    @Test
    fun toggleCompleteTwiceRestoresInitialState() {
        val repository = TaskRepository()
        repository.addTask(task(id = 1L, title = "First"))

        repository.toggleComplete(1L)
        repository.toggleComplete(1L)

        assertFalse(repository.tasks.value.single().completed)
    }

    @Test
    fun toggleCompleteWithUnknownIdDoesNotChangeTasks() {
        val repository = TaskRepository()
        val task = task(id = 1L, title = "First")
        repository.addTask(task)

        repository.toggleComplete(999L)

        assertEquals(listOf(task), repository.tasks.value)
    }

    @Test
    fun sortedByPriorityReturnsHighPriorityFirst() {
        val repository = TaskRepository()
        repository.addTask(task(id = 1L, title = "Low", priority = Priority.LOW))
        repository.addTask(task(id = 2L, title = "High", priority = Priority.HIGH))
        repository.addTask(task(id = 3L, title = "Medium", priority = Priority.MEDIUM))

        val sortedIds = repository.sortedByPriority().map { it.id }

        assertEquals(listOf(2L, 3L, 1L), sortedIds)
    }

    private fun task(
        id: Long,
        title: String,
        priority: Priority = Priority.MEDIUM,
    ): Task {
        return Task(
            id = id,
            title = title,
            priority = priority,
        )
    }
}
