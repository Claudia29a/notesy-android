package com.example.notesy.data.repository

import android.util.Log
import com.example.notesy.data.api.NoteApiService
import com.example.notesy.data.model.CreateNoteRequest
import com.example.notesy.data.model.Note
import kotlinx.coroutines.delay

class NoteRepository(private val apiService: NoteApiService) {

    private val mockNotes = mutableListOf<Note>()

    suspend fun getNotes(): List<Note> {
        return try {
            Log.d("NoteRepository", "Fetching notes from API...")
            delay(500) // Simulate network delay
            val response = apiService.getNotes()
            Log.d("NoteRepository", "Received ${response.size} notes")
            response
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error fetching notes, using mock data", e)
            mockNotes.toList()
        }
    }

    suspend fun createNote(request: CreateNoteRequest): Note {
        return try {
            Log.d("NoteRepository", "createNote called with title: ${request.title}")
            delay(500) // Simulate network delay
            val response = apiService.createNote(request)
            Log.d("NoteRepository", "Note created successfully from API")
            response
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error creating note via API, using mock", e)
            // Create mock note when API fails
            val mockNote = Note(
                id = System.currentTimeMillis().toString(),
                title = request.title,
                items = request.items,
                createdAt = "Just now"
            )
            mockNotes.add(mockNote)
            Log.d("NoteRepository", "Mock note created: ${mockNote.id}")
            mockNote
        }
    }

    suspend fun deleteNote(id: String) {
        try {
            Log.d("NoteRepository", "Deleting note with id: $id")
            apiService.deleteNote(id)
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error deleting note via API, removing from mock", e)
            mockNotes.removeIf { it.id == id }
        }
    }
}