package com.example.notesappcompose.ui.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.notesappcompose.ui.components.NoteCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NoteViewModel,
    onCreateNote: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Записи") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNote,
                modifier = Modifier.testTag("createNoteButton")
            ) {
                Text("+")
            }
        }
    ) { padding ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Пока нет записей",
                    modifier = Modifier.testTag("emptyNotesText"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .testTag("noteList"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notes) { note ->
                    NoteCard(
                        note = note,
                        onDelete = { viewModel.deleteNote(note.id) }
                    )
                }
            }
        }
    }
}
