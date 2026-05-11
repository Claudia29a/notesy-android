package com.example.notesy.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.notesy.data.api.NoteApiService
import com.example.notesy.data.local.NoteDao
import com.example.notesy.data.local.NoteEntity
import com.example.notesy.data.local.toEntity
import com.example.notesy.data.local.toNote
import com.example.notesy.data.model.CreateNoteRequest
import com.example.notesy.data.model.Note
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID

class NoteRepository(
    private val apiService: NoteApiService,
    private val noteDao: NoteDao
) {

    fun getAllNotesFlow(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { entities ->
            entities.map { it.toNote() }
        }
    }

    suspend fun syncWithBackend() {
        try {
            val unsyncedNotes = noteDao.getUnsyncedNotes()

            unsyncedNotes.forEach { localNote ->
                try {
                    val itemsList = Gson().fromJson(
                        localNote.items,
                        Array<String>::class.java
                    ).toList()

                    val createdNote = apiService.createNote(
                        CreateNoteRequest(
                            title = localNote.title,
                            items = itemsList
                        )
                    )

                    noteDao.deleteNoteById(localNote.id)
                    noteDao.insertNote(createdNote.toEntity(isSynced = true))
                } catch (_: Exception) {
                }
            }

            val notesFromApi = apiService.getNotes()
            val entities = notesFromApi.map { it.toEntity(isSynced = true) }
            noteDao.insertNotes(entities)
        } catch (_: Exception) {
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createNote(title: String, items: List<String>) {
        val localNote = NoteEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            items = Gson().toJson(items),
            createdAt = Instant.now().toString(),
            isSynced = false
        )

        noteDao.insertNote(localNote)

        try {
            val createdNote = apiService.createNote(
                CreateNoteRequest(title = title, items = items)
            )

            noteDao.deleteNoteById(localNote.id)
            noteDao.insertNote(createdNote.toEntity(isSynced = true))
        } catch (_: Exception) {
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateNote(id: String, title: String, items: List<String>) {
        val existingNote = noteDao.getNoteById(id)
        val updatedNote = NoteEntity(
            id = id,
            title = title,
            items = Gson().toJson(items),
            createdAt = existingNote?.createdAt ?: Instant.now().toString(),
            isSynced = false
        )

        noteDao.insertNote(updatedNote)

        try {
            val serverNote = apiService.updateNote(
                id = id,
                request = CreateNoteRequest(title = title, items = items)
            )

            noteDao.insertNote(serverNote.toEntity(isSynced = true))
        } catch (_: Exception) {
        }
    }

    suspend fun deleteNote(id: String) {
        noteDao.deleteNoteById(id)

        try {
            apiService.deleteNote(id)
        } catch (_: Exception) {
        }
    }
}