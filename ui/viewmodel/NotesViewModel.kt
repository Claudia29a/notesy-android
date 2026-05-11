package com.example.notesy.ui.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.notesy.data.api.RetrofitInstance
import com.example.notesy.data.local.NotesDatabase
import com.example.notesy.data.model.Note
import com.example.notesy.data.repository.NoteRepository
import com.example.notesy.data.worker.SyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NotesDatabase.getDatabase(application)
    private val repository = NoteRepository(RetrofitInstance.api, database.noteDao())

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _noteCreated = MutableStateFlow(false)
    val noteCreated: StateFlow<Boolean> = _noteCreated

    init {
        viewModelScope.launch {
            repository.getAllNotesFlow().collect { notesList ->
                _notes.value = notesList
                Log.d("NotesViewModel", "Notes updated from database: ${notesList.size}")
            }
        }

        syncWithBackend()
    }

    private fun syncWithBackend() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.syncWithBackend()
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Sync failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNote(title: String, items: List<String>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.createNote(title, items)
                scheduleSync()
                _noteCreated.value = true
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Failed to create note", e)
                _noteCreated.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateNote(id: String, title: String, items: List<String>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.updateNote(id, title, items)
                scheduleSync()
                _noteCreated.value = true
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Failed to update note", e)
                _noteCreated.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun scheduleSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(getApplication()).enqueue(request)
    }

    fun resetNoteCreated() {
        _noteCreated.value = false
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteNote(id)
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Error deleting note", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshNotes() {
        syncWithBackend()
    }
}