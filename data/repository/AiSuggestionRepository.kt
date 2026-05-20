package com.example.notesy.data.repository

import android.util.Log
import com.example.notesy.data.api.NoteApiService
import com.example.notesy.data.model.AiSuggestRequest

class AiSuggestionRepository(
    private val apiService: NoteApiService
) {
    suspend fun suggestGroceries(
        title: String,
        content: String,
        recentNotes: List<String> = emptyList()
    ): List<String> {
        val combinedText = buildString {
            appendLine("Title:")
            appendLine(title)
            appendLine()
            appendLine("Content:")
            appendLine(content)

            if (recentNotes.isNotEmpty()) {
                appendLine()
                appendLine("Recent notes:")
                recentNotes.forEachIndexed { index, note ->
                    appendLine("--- Note ${index + 1} ---")
                    appendLine(note)
                }
            }
        }.trim()

        return try {
            val response = apiService.suggestGroceries(
                AiSuggestRequest(noteText = combinedText)
            )

            response.items
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
        } catch (e: Exception) {
            Log.e("AI_SUGGESTIONS", "Failed to fetch suggestions from backend", e)
            emptyList()
        }
    }
}