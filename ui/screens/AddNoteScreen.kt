package com.example.notesy.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.notesy.ui.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    viewModel: NotesViewModel,
    onNavigateBack: () -> Unit,
    noteId: String? = null,
    folderId: String? = null
) {
    val notes by viewModel.notes.collectAsState()
    val existingNote = noteId?.let { id ->
        notes.find { it.id == id }
    }

    var title by remember(existingNote) { mutableStateOf(existingNote?.title ?: "") }

    // Use TextFieldValue so we can control cursor position
    var contentField by remember(existingNote) {
        mutableStateOf(
            TextFieldValue(
                text = existingNote?.content ?: "",
                selection = TextRange((existingNote?.content ?: "").length)
            )
        )
    }

    val noteCreated by viewModel.noteCreated.collectAsState()

    LaunchedEffect(noteCreated) {
        if (noteCreated) {
            Log.d("AddNoteScreen", "✅ Note saved successfully, navigating back")
            viewModel.resetNoteCreated()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingNote != null) "Edit Note" else "New Note") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val content = contentField.text
                            if (title.isNotBlank()) {
                                if (existingNote != null) {
                                    viewModel.updateNote(
                                        existingNote.id,
                                        title,
                                        content,
                                        existingNote.folderId
                                    )
                                } else {
                                    viewModel.createNote(title, content, folderId)
                                }
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Note Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Simple toolbar: bullet, checkbox, (fake) bold marker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Bullet point
                IconButton(onClick = {
                    contentField = insertAtLineStart(contentField, "• ")
                }) {
                    Icon(Icons.Default.FormatListBulleted, contentDescription = "Bullet")
                }

                // Checkbox (unchecked)
                IconButton(onClick = {
                    contentField = insertAtLineStart(contentField, "☐ ")
                }) {
                    Icon(Icons.Default.CheckBox, contentDescription = "Checkbox")
                }

                // Very simple "bold" marker using ** ** (Markdown style)
                IconButton(onClick = {
                    contentField = wrapSelection(contentField, "**")
                }) {
                    Icon(Icons.Default.FormatBold, contentDescription = "Bold markers")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = contentField,
                onValueChange = { contentField = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                minLines = 10,
                maxLines = Int.MAX_VALUE
            )
        }
    }
}

/**
 * Insert prefix (e.g. "• " or "☐ ") at the start of the current line.
 */
private fun insertAtLineStart(
    field: TextFieldValue,
    prefix: String
): TextFieldValue {
    val text = field.text
    val cursor = field.selection.start

    // find start of current line
    val lineStart = text.lastIndexOf('\n', startIndex = cursor - 1).let { index ->
        if (index == -1) 0 else index + 1
    }

    val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
    val newCursor = cursor + prefix.length

    return field.copy(
        text = newText,
        selection = TextRange(newCursor)
    )
}

/**
 * Wrap current selection with marker (e.g. **bold**).
 */
private fun wrapSelection(
    field: TextFieldValue,
    marker: String
): TextFieldValue {
    val text = field.text
    val start = field.selection.start
    val end = field.selection.end

    if (start == end) {
        // no selection; just insert markers
        val newText = text.substring(0, start) + marker + marker + text.substring(end)
        val newCursor = start + marker.length
        return field.copy(
            text = newText,
            selection = TextRange(newCursor)
        )
    }

    val newText =
        text.substring(0, start) + marker + text.substring(start, end) + marker + text.substring(
            end
        )
    val newCursor = end + 2 * marker.length

    return field.copy(
        text = newText,
        selection = TextRange(newCursor)
    )
}