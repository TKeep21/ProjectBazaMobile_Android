package com.example.notesappcompose.ui.task

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.notesappcompose.data.tasks.Priority
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TaskListScreenUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyTaskListShowsPlaceholderAndCreateAction() {
        val viewModel = TaskViewModel()
        var createClicked = false

        composeRule.setContent {
            MaterialTheme {
                TaskListScreen(
                    viewModel = viewModel,
                    onCreateTask = { createClicked = true },
                )
            }
        }

        composeRule.onNodeWithTag("emptyTasksText").assertExists()
        composeRule.onNodeWithText("Пока нет задач").assertExists()
        composeRule.onNodeWithTag("createTaskButton").performClick()

        assertTrue(createClicked)
    }

    @Test
    fun taskCheckboxTogglesCompletion() {
        val viewModel = TaskViewModel()
        viewModel.addTask(
            title = "Проверить тесты",
            description = "Перед коммитом",
            priority = Priority.HIGH,
            flagged = false,
            deadline = null,
        )
        val taskId = viewModel.tasks.value.single().id

        composeRule.setContent {
            MaterialTheme {
                TaskListScreen(
                    viewModel = viewModel,
                    onCreateTask = {},
                )
            }
        }

        composeRule.onNodeWithText("Проверить тесты").assertExists()
        composeRule.onNodeWithTag("taskComplete_$taskId").assertIsOff()
        composeRule.onNodeWithTag("taskComplete_$taskId").performClick()

        composeRule.runOnIdle {
            assertTrue(viewModel.tasks.value.single().completed)
        }
        composeRule.onNodeWithTag("taskComplete_$taskId").assertIsOn()
    }
}
