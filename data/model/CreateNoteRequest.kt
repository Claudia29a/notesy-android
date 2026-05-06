package com.example.notesy.data.model

data class CreateNoteRequest(
    val title: String,
    val items: List<String>
)