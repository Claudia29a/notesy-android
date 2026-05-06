package com.example.notesy.data.model

data class Note(
    val id: String,
    val title: String,
    val items: List<String>,
    val createdAt: String? = null,
    val updatedAt: String? = null
)