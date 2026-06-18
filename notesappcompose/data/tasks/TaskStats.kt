package com.example.notesappcompose.data.tasks

import java.time.LocalDateTime

data class TaskStats(
    val total: Int,
    val active: Int,
    val completed: Int,
    val flagged: Int,
    val overdue: Int,
    val dueSoon: Int,
    val highPriorityActive: Int,
    val nextDeadline: Task?,
) {
    val completionPercent: Int
        get() = if (total == 0) 0 else (completed * 100) / total
}

class TaskStatsCalculator(
    private val dueSoonHours: Long = 24L,
) {
    fun calculate(tasks: List<Task>, now: LocalDateTime): TaskStats {
        val activeTasks = tasks.filterNot { it.completed }
        val dueSoonLimit = now.plusHours(dueSoonHours)

        return TaskStats(
            total = tasks.size,
            active = activeTasks.size,
            completed = tasks.count { it.completed },
            flagged = activeTasks.count { it.flagged },
            overdue = activeTasks.count { task ->
                task.deadline?.isBefore(now) == true
            },
            dueSoon = activeTasks.count { task ->
                task.deadline?.let { deadline ->
                    !deadline.isBefore(now) && !deadline.isAfter(dueSoonLimit)
                } == true
            },
            highPriorityActive = activeTasks.count { it.priority == Priority.HIGH },
            nextDeadline = activeTasks
                .filter { it.deadline != null }
                .minByOrNull { it.deadline!! },
        )
    }
}
