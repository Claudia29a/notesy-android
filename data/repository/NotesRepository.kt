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

    fun getNotesByFolderFlow(folderId: String): Flow<List<Note>> {
        return noteDao.getNotesByFolder(folderId).map { entities ->
            entities.map { it.toNote() }
        }
    }

    fun getNotesWithoutFolderFlow(): Flow<List<Note>> {
        return noteDao.getNotesWithoutFolder().map { entities ->
            entities.map { it.toNote() }
        }
    }

    suspend fun syncWithBackend() {
        try {
            val unsyncedNotes = noteDao.getUnsyncedNotes()

            unsyncedNotes.forEach { localNote ->
                try {
                    val updatedNote = apiService.updateNote(
                        id = localNote.id,
                        request = CreateNoteRequest(
                            title = localNote.title,
                            content = localNote.content,
                            folderId = localNote.folderId
                        )
                    )
                    noteDao.insertNote(updatedNote.toEntity(isSynced = true))
                } catch (_: Exception) {
                    try {
                        val createdNote = apiService.createNote(
                            CreateNoteRequest(
                                title = localNote.title,
                                content = localNote.content,
                                folderId = localNote.folderId
                            )
                        )

                        noteDao.deleteNoteById(localNote.id)
                        noteDao.insertNote(createdNote.toEntity(isSynced = true))
                    } catch (_: Exception) {
                    }
                }
            }

            val notesFromApi = apiService.getNotes()
            val entities = notesFromApi.map { it.toEntity(isSynced = true) }
            noteDao.insertNotes(entities)
        } catch (_: Exception) {
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createNote(title: String, content: String, folderId: String? = null) {
        val localNote = NoteEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            folderId = folderId,
            createdAt = Instant.now().toString(),
            isSynced = false
        )

        noteDao.insertNote(localNote)

        try {
            val createdNote = apiService.createNote(
                CreateNoteRequest(
                    title = title,
                    content = content,
                    folderId = folderId
                )
            )

            noteDao.deleteNoteById(localNote.id)
            noteDao.insertNote(createdNote.toEntity(isSynced = true))
        } catch (_: Exception) {
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateNote(id: String, title: String, content: String, folderId: String? = null) {
        val existingNote = noteDao.getNoteById(id)
        val updatedNote = NoteEntity(
            id = id,
            title = title,
            content = content,
            folderId = folderId,
            createdAt = existingNote?.createdAt ?: Instant.now().toString(),
            isSynced = false
        )

        noteDao.insertNote(updatedNote)

        try {
            val serverNote = apiService.updateNote(
                id = id,
                request = CreateNoteRequest(
                    title = title,
                    content = content,
                    folderId = folderId
                )
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