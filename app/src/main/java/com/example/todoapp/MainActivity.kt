package com.example.todoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todoapp.ui.theme.TodoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TodoAppTheme {

                val todoItemList = remember {
                    mutableStateListOf(
                        TodoItem(
                            id = 1L,
                            todoTitle = "Task 1"
                        )
                    )
                }

                var showAddTodoDialog by remember {
                    mutableStateOf(false)
                }

                var showDeleteTodoDialog by remember {
                    mutableStateOf(false)
                }

                var todoTitle by remember {
                    mutableStateOf("")
                }

                var selectedTodoItem by remember {
                    mutableStateOf<TodoItem?>(null)
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

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {

                        itemsIndexed(todoItemList) { _, item ->

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Checkbox(
                                        checked = item.isCompleted,
                                        onCheckedChange = { newValue ->
                                            val updatedItem =
                                                item.copy(isCompleted = newValue)

                                            val itemIndex =
                                                todoItemList.indexOfFirst {
                                                    it.id == item.id
                                                }

                                            if (itemIndex != -1) {
                                                todoItemList[itemIndex] = updatedItem
                                            }
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
                                    todoItemList.add(
                                        TodoItem(
                                            id = System.currentTimeMillis(),
                                            todoTitle = todoTitle
                                        )
                                    )
                                }

                                showAddTodoDialog = false
                                todoTitle = ""
                                selectedTodoItem = null
                            },
                            onEditClicked = {

                                selectedTodoItem?.let { todoItem ->

                                    val updatedItem =
                                        todoItem.copy(todoTitle = todoTitle)

                                    val itemIndex =
                                        todoItemList.indexOfFirst {
                                            it.id == todoItem.id
                                        }

                                    if (itemIndex != -1) {
                                        todoItemList[itemIndex] = updatedItem
                                    }
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

                                    val itemIndex =
                                        todoItemList.indexOfFirst {
                                            it.id == todoItem.id
                                        }

                                    if (itemIndex != -1) {
                                        todoItemList.removeAt(itemIndex)
                                    }
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