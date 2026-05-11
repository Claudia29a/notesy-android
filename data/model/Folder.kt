package com.example.notesy.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Folder(
    val id: String,
    val name: String,
    val createdAt: String
)

@Serializable
data class CreateFolderRequest(
    val name: String
)