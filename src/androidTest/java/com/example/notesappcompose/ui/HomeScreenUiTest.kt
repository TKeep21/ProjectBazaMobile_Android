package com.example.notesappcompose.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.notesappcompose.data.tasks.Priority
import com.example.notesappcompose.ui.task.TaskViewModel
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class HomeScreenUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun overviewShowsAggregatedTaskStats() {
        val viewModel = TaskViewModel()
        val now = LocalDateTime.now()
        viewModel.addTask(
            title = "Просроченная задача",
            description = "Important",
            priority = Priority.HIGH,
            flagged = true,
            deadline = now.minusHours(1),
        )
        viewModel.addTask(
            title = "Скоро дедлайн",
            description = "Soon",
            priority = Priority.MEDIUM,
            flagged = false,
            deadline = now.plusHours(2),
        )
        viewModel.addTask(
            title = "Готовая задача",
            description = "Done",
            priority = Priority.LOW,
            flagged = false,
            deadline = null,
        )
        viewModel.toggleComplete(viewModel.tasks.value.last().id)

        composeRule.setContent {
            MaterialTheme {
                HomeScreen(viewModel = viewModel)
            }
        }

        composeRule.onNodeWithText("Всего задач: 3").assertExists()
        composeRule.onNodeWithText("Активных: 2").assertExists()
        composeRule.onNodeWithText("Завершено: 1").assertExists()
        composeRule.onNodeWithText("Прогресс: 33%").assertExists()
        composeRule.onNodeWithText("С флагом: 1").assertExists()
        composeRule.onNodeWithText("Просрочено: 1").assertExists()
        composeRule.onNodeWithText("На ближайшие 24 часа: 1").assertExists()
        composeRule.onNodeWithText("Высокий приоритет: 1").assertExists()
        composeRule.onNodeWithText("Просроченная задача").assertExists()
    }
}
