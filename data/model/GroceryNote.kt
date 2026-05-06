package com.example.notesy.data.model

data class GroceryNote(
    val id: String,
    val title: String,
    val items: List<GroceryItem>,
    val createdAt: Long,
    val updatedAt: Long
)

data class GroceryItem(
    val name: String,
    val isChecked: Boolean = false
)