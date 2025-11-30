package com.example.notesappcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.notesappcompose.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: TaskViewModel = viewModel()

            val nav = rememberNavController()

            NavHost(
                navController = nav,
                startDestination = "task_list"
            ) {
                composable("task_list") {
                    TaskListScreen(
                        viewModel = vm,
                        onAddClicked = { nav.navigate("create_task") }
                    )
                }

                composable("create_task") {
                    CreateTaskScreen(
                        viewModel = vm,
                        onSave = { nav.popBackStack() },
                        onCancel = { nav.popBackStack() }
                    )
                }
            }
        }
    }
}
