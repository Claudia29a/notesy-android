package com.example.notesy.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AiSuggestRequest(
    val noteText: String
)

@Serializable
data class AiSuggestResponse(
    val items: List<String>
)