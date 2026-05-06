package com.example.notesy.data.repository

import android.util.Log
import com.example.notesy.data.api.NoteApiService
import com.example.notesy.data.model.CreateNoteRequest
import com.example.notesy.data.model.Note

class NoteRepository(private val apiService: NoteApiService) {

    suspend fun getNotes(): List<Note> {
        return try {
            Log.d("NoteRepository", "Fetching notes from API...")
            val response = apiService.getNotes()
            Log.d("NoteRepository", "Received ${response.size} notes from backend")
            response
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error fetching notes", e)
            emptyList()
        }
    }

    suspend fun createNote(request: CreateNoteRequest): Note {
        Log.d("NoteRepository", "Creating note with title: ${request.title}")
        val response = apiService.createNote(request)
        Log.d("NoteRepository", "Note created successfully: ${response.id}")
        return response
    }

    suspend fun deleteNote(id: String) {
        Log.d("NoteRepository", "Deleting note with id: $id")
        apiService.deleteNote(id)
        Log.d("NoteRepository", "Note deleted successfully")
    }
}