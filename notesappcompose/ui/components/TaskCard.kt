package com.example.notesappcompose.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.notesappcompose.data.Task
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun TaskCard(
    task: Task,
    onToggleComplete: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        task.title,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (task.flagged) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Отмечено флагом",
                            tint = Color.Red,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Checkbox(checked = task.completed, onCheckedChange = { onToggleComplete() })
            }

            if (task.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(task.description, color = Color.Gray)
            }

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Приоритет: ${task.priority.label}",
                    color = Color.Blue,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize
                )

                if (task.deadline != null) {
                    Text(
                        text = "До: ${task.deadline.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))}",
                        color = Color.Gray,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                }
            }
        }
    }
}
