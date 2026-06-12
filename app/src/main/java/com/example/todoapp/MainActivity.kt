package com.example.todoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todoapp.data.TodoDatabase
import com.example.todoapp.data.TodoEntity
import com.example.todoapp.repository.TodoRepository
import com.example.todoapp.ui.theme.TodoAppTheme
import com.example.todoapp.viewmodel.TodoViewModel
import com.example.todoapp.viewmodel.TodoViewModelFactory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

class MainActivity : ComponentActivity() {
    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TodoAppTheme {

                var showAddTodoDialog by remember {
                    mutableStateOf(false)
                }

                var showDeleteTodoDialog by remember {
                    mutableStateOf(false)
                }

                var todoTitle by remember {
                    mutableStateOf("")
                }

                val database = remember {
                    TodoDatabase.getDatabase(applicationContext)
                }

                val repository = remember {
                    TodoRepository(database.todoDao())
                }

                val viewModel: TodoViewModel = viewModel(
                    factory = TodoViewModelFactory(repository)
                )

                val todoItemList by viewModel.todos.collectAsStateWithLifecycle()


                var selectedTodoItem by remember {
                    mutableStateOf<TodoEntity?>(null)
                }


                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                selectedTodoItem = null
                                todoTitle = ""
                                showAddTodoDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Todo"
                            )
                        }
                    }
                ) { innerPadding ->

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {


                        TopAppBar(
                            title = {
                                Text("Task Manager")
                            }
                        )

                        val completedTasks = todoItemList.count { it.isCompleted }

                        Text(
                            text = "Total Tasks: ${todoItemList.size}",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Text(
                            text = "Completed: $completedTasks",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {

                            if (todoItemList.isEmpty()) {
                                item {
                                    Text(
                                        text = "No tasks yet. Tap + to add one!",
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }

                            itemsIndexed(todoItemList) { _, item ->

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 4.dp
                                    )
                                ) {

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            Checkbox(
                                                checked = item.isCompleted,
                                                onCheckedChange = { newValue ->
                                                    viewModel.updateTodo(
                                                        item.copy(
                                                            isCompleted = newValue
                                                        )
                                                    )
                                                }
                                            )

                                            Text(text = item.todoTitle)
                                        }

                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Todo",
                                            modifier = Modifier.clickable {
                                                selectedTodoItem = item
                                                todoTitle = item.todoTitle
                                                showAddTodoDialog = true
                                            }
                                        )

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Todo",
                                            modifier = Modifier.clickable {
                                                selectedTodoItem = item
                                                showDeleteTodoDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                    if (showAddTodoDialog) {

                        AddTodoDialog(
                            onCancel = {
                                showAddTodoDialog = false
                                todoTitle = ""
                                selectedTodoItem = null
                            },
                            onTitleChanged = {
                                todoTitle = it
                            },
                            onAddClicked = {

                                if (todoTitle.isNotBlank()) {
                                    viewModel.addTodo(todoTitle)
                                }

                                showAddTodoDialog = false
                                todoTitle = ""
                                selectedTodoItem = null
                            },
                            onEditClicked = {

                                selectedTodoItem?.let { todoItem ->
                                    viewModel.updateTodo(
                                        todoItem.copy(
                                            todoTitle = todoTitle
                                        )
                                    )
                                }

                                showAddTodoDialog = false
                                todoTitle = ""
                                selectedTodoItem = null
                            },
                            todoTitle = todoTitle,
                            isAdd = selectedTodoItem == null
                        )
                    }

                    if (showDeleteTodoDialog) {

                        DeleteTodoDialog(
                            onCancel = {
                                showDeleteTodoDialog = false
                                selectedTodoItem = null
                            },
                            onDeleteClicked = {

                                selectedTodoItem?.let { todoItem ->
                                    viewModel.deleteTodo(todoItem)
                                }

                                showDeleteTodoDialog = false
                                selectedTodoItem = null
                            }
                        )
                    }
                }
            }
        }
    }


@Composable
fun AddTodoDialog(
    onCancel: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onAddClicked: () -> Unit,
    onEditClicked: () -> Unit,
    todoTitle: String,
    isAdd: Boolean
) {

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            TextButton(
                onClick = {
                    if (isAdd) {
                        onAddClicked()
                    } else {
                        onEditClicked()
                    }
                }
            ) {
                Text(if (isAdd) "Add" else "Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        },
        title = {
            Text(if (isAdd) "Add Todo" else "Edit Todo")
        },
        text = {
            OutlinedTextField(
                value = todoTitle,
                onValueChange = onTitleChanged,
                label = {
                    Text("Todo Title")
                }
            )
        }
    )
}

@Composable
fun DeleteTodoDialog(
    onCancel: () -> Unit,
    onDeleteClicked: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            TextButton(
                onClick = onDeleteClicked
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel
            ) {
                Text("Cancel")
            }
        },
        title = {
            Text("Delete Todo")
        },
        text = {
            Text("Do you want to delete this Todo item?")
        }
    )
}
