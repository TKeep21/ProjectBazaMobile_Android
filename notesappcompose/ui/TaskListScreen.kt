package com.example.notesappcompose.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import com.example.notesappcompose.Task


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onAddClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Задачи") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClicked) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {

            if (viewModel.tasks.isEmpty()) {
                Text(
                    "Задач нет",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(viewModel.tasks) { task ->
                        TaskRow(task)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskRow(task: Task) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(task.title, style = MaterialTheme.typography.titleMedium)

        if (!task.details.isNullOrEmpty()) {
            Text(
                task.details ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}