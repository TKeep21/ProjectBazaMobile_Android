package com.example.notesappcompose.data

import com.example.notesappcompose.data.tasks.Priority
import com.example.notesappcompose.data.tasks.Task
import com.example.notesappcompose.data.tasks.TaskStatsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class TaskStatsCalculatorTest {
    private val now = LocalDateTime.of(2026, 6, 16, 12, 0)
    private val calculator = TaskStatsCalculator()

    @Test
    fun calculateReturnsEmptyStatsForEmptyTaskList() {
        val stats = calculator.calculate(tasks = emptyList(), now = now)

        assertEquals(0, stats.total)
        assertEquals(0, stats.active)
        assertEquals(0, stats.completed)
        assertEquals(0, stats.flagged)
        assertEquals(0, stats.completionPercent)
        assertNull(stats.nextDeadline)
    }

    @Test
    fun calculateSeparatesActiveCompletedAndFocusMetrics() {
        val tasks = listOf(
            task(id = 1L, priority = Priority.HIGH, flagged = true, deadline = now.minusHours(1)),
            task(id = 2L, priority = Priority.HIGH, deadline = now.plusHours(2)),
            task(id = 3L, priority = Priority.LOW, completed = true, deadline = now.minusDays(1)),
            task(id = 4L, priority = Priority.MEDIUM),
        )

        val stats = calculator.calculate(tasks = tasks, now = now)

        assertEquals(4, stats.total)
        assertEquals(3, stats.active)
        assertEquals(1, stats.completed)
        assertEquals(25, stats.completionPercent)
        assertEquals(1, stats.flagged)
        assertEquals(1, stats.overdue)
        assertEquals(1, stats.dueSoon)
        assertEquals(2, stats.highPriorityActive)
        assertEquals(1L, stats.nextDeadline?.id)
    }

    @Test
    fun calculateDoesNotCountCompletedTasksInFocusMetrics() {
        val tasks = listOf(
            task(id = 1L, priority = Priority.HIGH, flagged = true, completed = true, deadline = now.minusHours(1)),
            task(id = 2L, priority = Priority.MEDIUM, deadline = now.plusHours(25)),
        )

        val stats = calculator.calculate(tasks = tasks, now = now)

        assertEquals(0, stats.flagged)
        assertEquals(0, stats.overdue)
        assertEquals(0, stats.dueSoon)
        assertEquals(0, stats.highPriorityActive)
        assertEquals(2L, stats.nextDeadline?.id)
    }

    @Test
    fun calculateCountsDueSoonBoundaryInclusively() {
        val tasks = listOf(
            task(id = 1L, deadline = now),
            task(id = 2L, deadline = now.plusHours(24)),
            task(id = 3L, deadline = now.plusHours(24).plusMinutes(1)),
        )

        val stats = calculator.calculate(tasks = tasks, now = now)

        assertEquals(2, stats.dueSoon)
        assertEquals(1L, stats.nextDeadline?.id)
    }

    @Test
    fun calculateUsesEarliestActiveDeadline() {
        val tasks = listOf(
            task(id = 1L, completed = true, deadline = now.minusDays(2)),
            task(id = 2L, deadline = now.plusDays(3)),
            task(id = 3L, deadline = now.plusHours(5)),
        )

        val stats = calculator.calculate(tasks = tasks, now = now)

        assertEquals(3L, stats.nextDeadline?.id)
    }

    private fun task(
        id: Long,
        priority: Priority = Priority.MEDIUM,
        flagged: Boolean = false,
        deadline: LocalDateTime? = null,
        completed: Boolean = false,
    ): Task {
        return Task(
            id = id,
            title = "Task $id",
            priority = priority,
            flagged = flagged,
            deadline = deadline,
            completed = completed,
        )
    }
}
