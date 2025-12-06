package com.example.notesappcompose.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NoteRepository {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    fun addNote(note: Note) {
        _notes.value = _notes.value + note
    }

    fun deleteNote(id: Long) {
        _notes.value = _notes.value.filter { it.id != id }
    }
}

