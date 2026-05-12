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
import com.example.notesy.data.model.Folder
import com.example.notesy.data.model.Note
import com.example.notesy.data.repository.FolderRepository
import com.example.notesy.data.repository.NoteRepository
import com.example.notesy.data.worker.SyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NotesDatabase.getDatabase(application)
    private val noteRepository = NoteRepository(RetrofitInstance.api, database.noteDao())
    private val folderRepository = FolderRepository(RetrofitInstance.api, database.folderDao())

    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    val folders: StateFlow<List<Folder>> = _folders

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _noteCreated = MutableStateFlow(false)
    val noteCreated: StateFlow<Boolean> = _noteCreated

    private val _folderCreated = MutableStateFlow(false)
    val folderCreated: StateFlow<Boolean> = _folderCreated

    init {
        viewModelScope.launch {
            folderRepository.getAllFoldersFlow().collect { foldersList ->
                _folders.value = foldersList
                Log.d("NotesViewModel", "Folders updated from database: ${foldersList.size}")
            }
        }

        viewModelScope.launch {
            noteRepository.getAllNotesFlow().collect { notesList ->
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
                folderRepository.syncWithBackend()
                noteRepository.syncWithBackend()
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Sync failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createFolder(name: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                folderRepository.createFolder(name)
                scheduleSync()
                _folderCreated.value = true
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Failed to create folder", e)
                _folderCreated.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateFolder(id: String, name: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                folderRepository.updateFolder(id, name)
                scheduleSync()
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Failed to update folder", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteFolder(id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                folderRepository.deleteFolder(id)
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Error deleting folder", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNote(title: String, content: String, folderId: String? = null) {  // Changed from items: List<String>
        viewModelScope.launch {
            try {
                _isLoading.value = true
                noteRepository.createNote(title, content, folderId)  // Changed from items
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
    fun updateNote(id: String, title: String, content: String, folderId: String? = null) {  // Changed from items: List<String>
        viewModelScope.launch {
            try {
                _isLoading.value = true
                noteRepository.updateNote(id, title, content, folderId)  // Changed from items
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

    fun resetFolderCreated() {
        _folderCreated.value = false
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                noteRepository.deleteNote(id)
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