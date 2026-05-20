package com.example.notesy.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.notesy.ui.viewmodel.NotesViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    viewModel: NotesViewModel,
    onNavigateBack: () -> Unit,
    noteId: String? = null,
    folderId: String? = null
) {
    val notes by viewModel.notes.collectAsState()
    val noteCreated by viewModel.noteCreated.collectAsState()
    val groceryDialogState by viewModel.groceryDialogState.collectAsState()

    val existingNote = noteId?.let { id ->
        notes.find { it.id == id }
    }

    var title by remember(existingNote) {
        mutableStateOf(existingNote?.title ?: "")
    }

    var contentField by remember(existingNote) {
        mutableStateOf(
            TextFieldValue(
                text = existingNote?.content ?: "",
                selection = TextRange((existingNote?.content ?: "").length)
            )
        )
    }

    val contentScrollState = rememberScrollState()

    LaunchedEffect(noteCreated) {
        if (noteCreated) {
            Log.d("AddNoteScreen", "Note saved successfully, navigating back")
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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (existingNote != null) {
                        IconButton(
                            onClick = {
                                viewModel.onSuggestGroceriesClicked(existingNote.id)
                            }
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "AI suggestions"
                            )
                        }
                    }

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
                .imePadding()
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        contentField = insertAtLineStart(contentField, "• ")
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.FormatListBulleted,
                        contentDescription = "Bullet"
                    )
                }

                IconButton(
                    onClick = {
                        contentField = insertAtLineStart(contentField, "☐ ")
                    }
                ) {
                    Icon(Icons.Default.CheckBox, contentDescription = "Checkbox")
                }

                IconButton(
                    onClick = {
                        contentField = wrapSelection(contentField, "**")
                    }
                ) {
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
                    .weight(1f)
                    .verticalScroll(contentScrollState),
                minLines = 10,
                maxLines = Int.MAX_VALUE
            )
        }

        if (groceryDialogState.visible && existingNote != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissGrocerySuggestions() },
                title = { Text("AI suggestions") },
                text = {
                    if (groceryDialogState.suggestions.isEmpty()) {
                        Text("No suggestions found.")
                    } else {
                        Column {
                            TextButton(
                                onClick = { viewModel.onSelectAllSuggestedItems() }
                            ) {
                                Text("Select all")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier.heightIn(max = 320.dp)
                            ) {
                                items(groceryDialogState.suggestions) { suggestion ->
                                    ListItem(
                                        headlineContent = {
                                            Text(suggestion.itemName)
                                        },
                                        supportingContent = {
                                            Text(suggestion.storeName)
                                        },
                                        leadingContent = {
                                            Checkbox(
                                                checked = groceryDialogState.selectedKeys.contains(
                                                    suggestion.key
                                                ),
                                                onCheckedChange = {
                                                    viewModel.onToggleSuggestedItem(suggestion.key)
                                                }
                                            )
                                        }
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.addSelectedSuggestedItems(existingNote.id) }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.dismissGrocerySuggestions() }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

private fun insertAtLineStart(
    field: TextFieldValue,
    prefix: String
): TextFieldValue {
    val text = field.text
    val cursor = field.selection.start

    val safeCursor = cursor.coerceAtLeast(0)
    val lineStart = text.lastIndexOf('\n', startIndex = (safeCursor - 1).coerceAtLeast(0)).let { index ->
        if (index == -1) 0 else index + 1
    }

    val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
    val newCursor = safeCursor + prefix.length

    return field.copy(
        text = newText,
        selection = TextRange(newCursor)
    )
}

private fun wrapSelection(
    field: TextFieldValue,
    marker: String = "**"
): TextFieldValue {
    val text = field.text
    val start = field.selection.start
    val end = field.selection.end

    if (start == end) {
        val newText = text.substring(0, start) + marker + marker + text.substring(end)
        val newCursor = start + marker.length
        return field.copy(
            text = newText,
            selection = TextRange(newCursor)
        )
    }

    val newText =
        text.substring(0, start) + marker + text.substring(start, end) + marker + text.substring(end)

    val newCursor = end + 2 * marker.length

    return field.copy(
        text = newText,
        selection = TextRange(newCursor)
    )
}