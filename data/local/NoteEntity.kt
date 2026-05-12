package com.example.notesy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,  // Changed from items
    val folderId: String?,
    val createdAt: String,
    val isSynced: Boolean = false  // Add this field
)