package com.example.notesappcompose.ui.notes

import androidx.lifecycle.ViewModel
import com.example.notesappcompose.data.notes.Note
import com.example.notesappcompose.data.notes.NoteRepository
import kotlinx.coroutines.flow.StateFlow

class NoteViewModel : ViewModel() {
    private val repo = NoteRepository()

    val notes: StateFlow<List<Note>> = repo.notes

    fun addNote(title: String, content: String) {
        val note = Note(
            title = title,
            content = content
        )
        repo.addNote(note)
    }

    fun deleteNote(id: Long) {
        repo.deleteNote(id)
    }
}