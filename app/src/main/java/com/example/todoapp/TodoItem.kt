package com.example.todoapp

data class TodoItem(
    val id : Long,
    val todoTitle : String,
    val isCompleted : Boolean = false
)