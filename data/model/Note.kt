package com.example.notesy.data.model

import com.google.gson.annotations.SerializedName

data class Note(
    val id: String,
    val title: String,
    val items: List<String>,
    @SerializedName("createdAt")
    val createdAt: String
)