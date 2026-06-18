package com.example.notesappcompose.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import com.example.notesappcompose.data.tasks.TaskStatsCalculator
import com.example.notesappcompose.ui.task.TaskViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TaskViewModel
) {
    val tasks by viewModel.tasks.collectAsState()
    val statsCalculator = remember { TaskStatsCalculator() }
    val stats = remember(tasks) {
        statsCalculator.calculate(tasks = tasks, now = LocalDateTime.now())
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Обзор") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Сводка по задачам",
                style = MaterialTheme.typography.headlineMedium
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Статистика",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Всего задач: ${stats.total}")
                    Text("Активных: ${stats.active}")
                    Text("Завершено: ${stats.completed}")
                    Text("Прогресс: ${stats.completionPercent}%")
                    Text("С флагом: ${stats.flagged}")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Фокус",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Просрочено: ${stats.overdue}")
                    Text("На ближайшие 24 часа: ${stats.dueSoon}")
                    Text("Высокий приоритет: ${stats.highPriorityActive}")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ближайший дедлайн",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(12.dp))
                    val nextTask = stats.nextDeadline
                    if (nextTask == null) {
                        Text("Нет активных задач с датой.")
                    } else {
                        Text(nextTask.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            nextTask.deadline!!.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
