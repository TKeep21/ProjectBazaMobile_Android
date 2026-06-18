package com.example.notesappcompose.ui.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.notesappcompose.ui.components.TaskCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onCreateTask: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Задачи") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTask,
                modifier = Modifier.testTag("createTaskButton")
            ) {
                Text("+")
            }
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Пока нет задач",
                    modifier = Modifier.testTag("emptyTasksText"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .testTag("taskList"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        onToggleComplete = { viewModel.toggleComplete(task.id) }
                    )
                }
            }
        }
    }
}
