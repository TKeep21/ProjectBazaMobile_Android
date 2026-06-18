package com.example.notesappcompose.ui.notes

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NotesScreenUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun createNoteScreenSavesEnteredNote() {
        val viewModel = NoteViewModel()
        var saved = false

        composeRule.setContent {
            MaterialTheme {
                CreateNoteScreen(
                    viewModel = viewModel,
                    onSave = { saved = true },
                )
            }
        }

        composeRule.onNodeWithTag("noteTitleInput").performTextInput("Идея")
        composeRule.onNodeWithTag("noteContentInput").performTextInput("Текст заметки")
        composeRule.onNodeWithTag("saveNoteButton").performClick()

        composeRule.runOnIdle {
            val note = viewModel.notes.value.single()
            assertEquals("Идея", note.title)
            assertEquals("Текст заметки", note.content)
            assertTrue(saved)
        }
    }

    @Test
    fun notesScreenShowsAndDeletesExistingNote() {
        val viewModel = NoteViewModel()
        viewModel.addNote(title = "Удаляемая заметка", content = "Тело")
        val noteId = viewModel.notes.value.single().id

        composeRule.setContent {
            MaterialTheme {
                NotesScreen(
                    viewModel = viewModel,
                    onCreateNote = {},
                )
            }
        }

        composeRule.onNodeWithText("Удаляемая заметка").assertExists()
        composeRule.onNodeWithTag("deleteNote_$noteId").performClick()

        composeRule.runOnIdle {
            assertTrue(viewModel.notes.value.isEmpty())
        }
        composeRule.onNodeWithTag("emptyNotesText").assertExists()
    }
}
