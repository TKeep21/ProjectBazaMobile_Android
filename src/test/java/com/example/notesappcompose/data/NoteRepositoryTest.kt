package com.example.notesappcompose.data

import com.example.notesappcompose.data.notes.Note
import com.example.notesappcompose.data.notes.NoteRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteRepositoryTest {
    @Test
    fun addNoteAppendsNoteToState() {
        val repository = NoteRepository()
        val note = note(id = 1L, title = "Idea")

        repository.addNote(note)

        assertEquals(listOf(note), repository.notes.value)
    }

    @Test
    fun deleteNoteRemovesOnlyMatchingNote() {
        val repository = NoteRepository()
        repository.addNote(note(id = 1L, title = "First"))
        repository.addNote(note(id = 2L, title = "Second"))

        repository.deleteNote(1L)

        assertEquals(listOf(2L), repository.notes.value.map { it.id })
    }

    @Test
    fun deleteNoteWithUnknownIdDoesNotChangeState() {
        val repository = NoteRepository()
        val note = note(id = 1L, title = "First")
        repository.addNote(note)

        repository.deleteNote(999L)

        assertEquals(listOf(note), repository.notes.value)
    }

    private fun note(id: Long, title: String): Note {
        return Note(
            id = id,
            title = title,
            content = "Content $id",
        )
    }
}
