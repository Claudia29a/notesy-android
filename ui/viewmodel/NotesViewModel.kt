package com.example.notesy.ui.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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
import com.example.notesy.data.repository.AiSuggestionRepository
import com.example.notesy.data.repository.FolderRepository
import com.example.notesy.data.repository.NoteRepository
import com.example.notesy.data.worker.SyncWorker
import com.example.notesy.utils.grocery.GrocerySuggestionDialogState
import com.example.notesy.utils.grocery.SuggestedItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NotesDatabase.getDatabase(application)
    private val noteRepository = NoteRepository(RetrofitInstance.api, database.noteDao())
    private val folderRepository = FolderRepository(RetrofitInstance.api, database.folderDao())
    private val aiSuggestionRepository = AiSuggestionRepository(RetrofitInstance.api)

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

    private val _groceryDialogState = MutableStateFlow(GrocerySuggestionDialogState())
    val groceryDialogState: StateFlow<GrocerySuggestionDialogState> = _groceryDialogState

    private val notesJson = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    private enum class BlockType {
        PARAGRAPH,
        BULLET,
        CHECKBOX,
        HEADING1,
        HEADING2,
        HEADING3
    }

    @Serializable
    private data class NoteBlockDto(
        val type: String,
        val text: String,
        val checked: Boolean? = null
    )

    private data class NoteBlockUi(
        val type: BlockType,
        val value: TextFieldValue,
        val checked: Boolean? = null
    )

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
                scheduleSync()
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Error deleting folder", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNote(title: String, content: String, folderId: String? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                noteRepository.createNote(title, content, folderId)
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
    fun updateNote(id: String, title: String, content: String, folderId: String? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                noteRepository.updateNote(id, title, content, folderId)
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

    fun onSuggestGroceriesClicked(currentNoteId: String) {
        viewModelScope.launch {
            try {
                val currentNoteEntity = database.noteDao().getNoteById(currentNoteId)

                if (currentNoteEntity == null) {
                    _groceryDialogState.value = GrocerySuggestionDialogState(
                        visible = true,
                        suggestions = emptyList(),
                        selectedKeys = emptySet()
                    )
                    return@launch
                }

                val recentNotes = notes.value
                    .filter { it.id != currentNoteId }
                    .takeLast(5)
                    .map { "${it.title}\n${it.content}" }

                val suggestions = aiSuggestionRepository.suggestGroceries(
                    title = currentNoteEntity.title,
                    content = currentNoteEntity.content,
                    recentNotes = recentNotes
                )

                val mappedSuggestions = suggestions.map { itemName ->
                    SuggestedItem(
                        storeName = "Suggested",
                        itemName = itemName,
                        timesSeen = 1
                    )
                }

                _groceryDialogState.value = GrocerySuggestionDialogState(
                    visible = true,
                    suggestions = mappedSuggestions,
                    selectedKeys = mappedSuggestions.map { it.key }.toSet()
                )
            } catch (e: Exception) {
                Log.e("AI_SUGGESTIONS", "Failed to get AI suggestions", e)
                _groceryDialogState.value = GrocerySuggestionDialogState(
                    visible = true,
                    suggestions = emptyList(),
                    selectedKeys = emptySet()
                )
            }
        }
    }

    fun onToggleSuggestedItem(key: String) {
        val current = _groceryDialogState.value
        val updated = current.selectedKeys.toMutableSet()

        if (updated.contains(key)) {
            updated.remove(key)
        } else {
            updated.add(key)
        }

        _groceryDialogState.value = current.copy(selectedKeys = updated)
    }

    fun onSelectAllSuggestedItems() {
        val current = _groceryDialogState.value
        _groceryDialogState.value = current.copy(
            selectedKeys = current.suggestions.map { it.key }.toSet()
        )
    }

    fun dismissGrocerySuggestions() {
        _groceryDialogState.value = GrocerySuggestionDialogState()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addSelectedSuggestedItems(currentNoteId: String) {
        viewModelScope.launch {
            try {
                val currentNoteEntity = database.noteDao().getNoteById(currentNoteId) ?: return@launch

                val dialogState = _groceryDialogState.value
                val selectedSuggestions = dialogState.suggestions.filter {
                    dialogState.selectedKeys.contains(it.key)
                }

                if (selectedSuggestions.isEmpty()) {
                    dismissGrocerySuggestions()
                    return@launch
                }

                val newItems = selectedSuggestions
                    .map { it.itemName.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()

                val updatedContent = appendSuggestedItemsAsBlocks(
                    existingContent = currentNoteEntity.content,
                    newItems = newItems
                )

                noteRepository.updateNote(
                    id = currentNoteEntity.id,
                    title = currentNoteEntity.title,
                    content = updatedContent,
                    folderId = currentNoteEntity.folderId
                )

                scheduleSync()
                dismissGrocerySuggestions()
            } catch (e: Exception) {
                Log.e("AI_SUGGESTIONS", "Failed to add selected suggested items", e)
            }
        }
    }

    private fun appendSuggestedItemsAsBlocks(
        existingContent: String,
        newItems: List<String>
    ): String {
        val blocks = parseBlocks(existingContent).toMutableList()

        if (blocks.isNotEmpty() && blocks.last().value.text.isNotBlank()) {
            blocks.add(
                NoteBlockUi(
                    type = BlockType.PARAGRAPH,
                    value = TextFieldValue("", TextRange(0))
                )
            )
        }

        blocks.add(
            NoteBlockUi(
                type = BlockType.HEADING3,
                value = TextFieldValue("Suggested items", TextRange("Suggested items".length))
            )
        )

        newItems.forEach { item ->
            blocks.add(
                NoteBlockUi(
                    type = BlockType.CHECKBOX,
                    value = TextFieldValue(item, TextRange(item.length)),
                    checked = false
                )
            )
        }

        return serializeBlocks(blocks)
    }

    private fun serializeBlocks(blocks: List<NoteBlockUi>): String {
        val dto = blocks.map {
            NoteBlockDto(
                type = it.type.name,
                text = it.value.text,
                checked = it.checked
            )
        }
        return notesJson.encodeToString(dto)
    }

    private fun parseBlocks(content: String): List<NoteBlockUi> {
        if (content.isBlank()) return emptyList()

        return try {
            val dto = notesJson.decodeFromString<List<NoteBlockDto>>(content)
            dto.map {
                NoteBlockUi(
                    type = it.type.toBlockType(),
                    value = TextFieldValue(it.text, TextRange(it.text.length)),
                    checked = it.checked
                )
            }
        } catch (_: Exception) {
            parseLegacyPlainText(content)
        }
    }

    private fun parseLegacyPlainText(content: String): List<NoteBlockUi> {
        if (content.isBlank()) return emptyList()

        return content.lines().map { line ->
            when {
                line.startsWith("☑ ") -> {
                    val text = line.removePrefix("☑ ")
                    NoteBlockUi(
                        type = BlockType.CHECKBOX,
                        value = TextFieldValue(text, TextRange(text.length)),
                        checked = true
                    )
                }
                line.startsWith("☐ ") -> {
                    val text = line.removePrefix("☐ ")
                    NoteBlockUi(
                        type = BlockType.CHECKBOX,
                        value = TextFieldValue(text, TextRange(text.length)),
                        checked = false
                    )
                }
                line.startsWith("• ") -> {
                    val text = line.removePrefix("• ")
                    NoteBlockUi(
                        type = BlockType.BULLET,
                        value = TextFieldValue(text, TextRange(text.length))
                    )
                }
                line.startsWith("### ") -> {
                    val text = line.removePrefix("### ")
                    NoteBlockUi(
                        type = BlockType.HEADING3,
                        value = TextFieldValue(text, TextRange(text.length))
                    )
                }
                line.startsWith("## ") -> {
                    val text = line.removePrefix("## ")
                    NoteBlockUi(
                        type = BlockType.HEADING2,
                        value = TextFieldValue(text, TextRange(text.length))
                    )
                }
                line.startsWith("# ") -> {
                    val text = line.removePrefix("# ")
                    NoteBlockUi(
                        type = BlockType.HEADING1,
                        value = TextFieldValue(text, TextRange(text.length))
                    )
                }
                else -> NoteBlockUi(
                    type = BlockType.PARAGRAPH,
                    value = TextFieldValue(line, TextRange(line.length))
                )
            }
        }
    }

    private fun String.toBlockType(): BlockType {
        return when (this) {
            "HEADING1" -> BlockType.HEADING1
            "HEADING2" -> BlockType.HEADING2
            "HEADING3" -> BlockType.HEADING3
            "BULLET" -> BlockType.BULLET
            "CHECKBOX" -> BlockType.CHECKBOX
            else -> BlockType.PARAGRAPH
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
                scheduleSync()
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