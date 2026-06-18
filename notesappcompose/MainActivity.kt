package com.example.notesappcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.notesappcompose.ui.news.NewsScreen
import com.example.notesappcompose.ui.news.NewsViewModel
import com.example.notesappcompose.ui.notes.CreateNoteScreen
import com.example.notesappcompose.ui.HomeScreen
import com.example.notesappcompose.ui.notes.NoteViewModel
import com.example.notesappcompose.ui.notes.NotesScreen
import com.example.notesappcompose.ui.task.CreateTaskScreen
import com.example.notesappcompose.ui.task.TaskListScreen
import com.example.notesappcompose.ui.task.TaskViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val taskViewModel: TaskViewModel = viewModel()
            val noteViewModel: NoteViewModel = viewModel()
            val newsViewModel: NewsViewModel = viewModel()
            val navController = rememberNavController()

            val items = listOf(
                TabItem("Обзор", "home", Icons.Default.Dashboard),
                TabItem("Новости", "news", Icons.AutoMirrored.Filled.Article),
                TabItem("Записи", "notes", Icons.Default.Edit),
                TabItem("Задачи", "tasks", Icons.Default.CheckCircle),
            )

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            Scaffold(
                bottomBar = {
                    NavigationBar {
                        items.forEach { item ->
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title) },
                                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.padding(padding)
                ) {
                    composable("home") {
                        HomeScreen(viewModel = taskViewModel)
                    }

                    composable("news") {
                        NewsScreen(viewModel = newsViewModel)
                    }

                    composable("tasks") {
                        TaskListScreen(
                            viewModel = taskViewModel,
                            onCreateTask = { navController.navigate("create_task") }
                        )
                    }

                    composable("create_task") {
                        CreateTaskScreen(
                            viewModel = taskViewModel,
                            onSave = { navController.popBackStack() }
                        )
                    }

                    composable("notes") {
                        NotesScreen(
                            viewModel = noteViewModel,
                            onCreateNote = { navController.navigate("create_note") }
                        )
                    }

                    composable("create_note") {
                        CreateNoteScreen(
                            viewModel = noteViewModel,
                            onSave = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

private data class TabItem(
    val title: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
