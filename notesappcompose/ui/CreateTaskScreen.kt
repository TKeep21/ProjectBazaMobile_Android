package com.example.notesappcompose.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.example.notesappcompose.data.Priority

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
                            val localDate = java.time.Instant
                                .ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Text("Приоритет:")
            Row {
                Priority.values().forEach { p ->
                    AssistChip(
                        onClick = { priority = p },
                        label = { Text(p.label) },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (priority == p) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = flagged, onCheckedChange = { flagged = it })
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
                    }
                )
                Text("Установить дату")
            }

            if (selectedDate != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Выбранная дата: ${selectedDate!!.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}",
                    color = MaterialTheme.colorScheme.primary
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
        }
    }
}
