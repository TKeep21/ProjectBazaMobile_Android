package com.example.notesappcompose.ui.task

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.example.notesappcompose.data.tasks.Priority
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    viewModel: TaskViewModel,
    onSave: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var flagged by remember { mutableStateOf(false) }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var pickDate by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    var selectedDate: LocalDateTime? by remember { mutableStateOf(null) }

    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = Instant
                                .ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            selectedDate = LocalDateTime.of(localDate, LocalTime.of(23, 59))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            },
            text = {
                DatePicker(state = datePickerState)
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Новая задача") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название задачи") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("taskTitleInput")
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("taskDescriptionInput")
            )

            Spacer(Modifier.height(12.dp))

            Text("Приоритет:")
            Row {
                Priority.values().forEach { p ->
                    AssistChip(
                        onClick = { priority = p },
                        label = { Text(p.label) },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("priority_${p.name}"),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (priority == p) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = flagged,
                    onCheckedChange = { flagged = it },
                    modifier = Modifier.testTag("taskFlagCheckbox")
                )
                Text("Отметить флагом")
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = pickDate,
                    onCheckedChange = {
                        pickDate = it
                        if (it) {
                            showDatePicker = true
                        } else {
                            selectedDate = null
                        }
                    },
                    modifier = Modifier.testTag("taskDateCheckbox")
                )
                Text("Установить дату")
            }

            selectedDate?.let { date ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Выбранная дата: ${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}",
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addTask(title, description, priority, flagged, selectedDate)
                        onSave()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("saveTaskButton")
            ) {
                Text("Сохранить")
            }
        }
    }
}
