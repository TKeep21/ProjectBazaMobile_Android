package com.example.notesappcompose.ui.task

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.notesappcompose.data.tasks.Priority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CreateTaskScreenUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun saveCreatesTaskWithEnteredFields() {
        val viewModel = TaskViewModel()
        var saved = false

        composeRule.setContent {
            MaterialTheme {
                CreateTaskScreen(
                    viewModel = viewModel,
                    onSave = { saved = true },
                )
            }
        }

        composeRule.onNodeWithTag("taskTitleInput").performTextInput("UI задача")
        composeRule.onNodeWithTag("taskDescriptionInput").performTextInput("Описание из теста")
        composeRule.onNodeWithTag("priority_HIGH").performClick()
        composeRule.onNodeWithTag("taskFlagCheckbox").performClick()
        composeRule.onNodeWithTag("saveTaskButton").performClick()

        composeRule.runOnIdle {
            val task = viewModel.tasks.value.single()
            assertEquals("UI задача", task.title)
            assertEquals("Описание из теста", task.description)
            assertEquals(Priority.HIGH, task.priority)
            assertTrue(task.flagged)
            assertTrue(saved)
        }
    }
}
