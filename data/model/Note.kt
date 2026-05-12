package com.example.notesy.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val folderId: String?,
    val createdAt: String
)

@Serializable
data class CreateNoteRequest(
    val title: String,
    val content: String,
    val folderId: String? = null
)