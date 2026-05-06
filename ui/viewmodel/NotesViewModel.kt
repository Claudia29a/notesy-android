package com.example.notesy.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesy.data.api.RetrofitInstance
import com.example.notesy.data.model.CreateNoteRequest
import com.example.notesy.data.model.Note
import com.example.notesy.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotesViewModel : ViewModel() {

    private val repository = NoteRepository(RetrofitInstance.api)

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _noteCreated = MutableStateFlow(false)
    val noteCreated: StateFlow<Boolean> = _noteCreated

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("NotesViewModel", "Loading notes...")
                val notesList = repository.getNotes()
                _notes.value = notesList
                Log.d("NotesViewModel", "Loaded ${notesList.size} notes")
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Error loading notes", e)
                _notes.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createNote(title: String, items: List<String>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("NotesViewModel", "Creating note: $title with ${items.size} items")
                val request = CreateNoteRequest(title, items)
                repository.createNote(request)
                Log.d("NotesViewModel", "Note created successfully")
                _noteCreated.value = true
                loadNotes()
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Failed to create note", e)
                _noteCreated.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetNoteCreated() {
        _noteCreated.value = false
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("NotesViewModel", "Deleting note: $id")
                repository.deleteNote(id)
                loadNotes()
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Error deleting note", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}