package com.example.notesy.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    var items by remember(existingNote) {
        mutableStateOf(existingNote?.items?.toList() ?: listOf(""))
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
                            if (title.isNotBlank() && items.any { it.isNotBlank() }) {
                                val validItems = items.filter { it.isNotBlank() }

                                if (existingNote != null) {
                                    viewModel.updateNote(
                                        existingNote.id,
                                        title,
                                        validItems,
                                        existingNote.folderId
                                    )
                                } else {
                                    viewModel.createNote(title, validItems, folderId)
                                }
                            }
                        },
                        enabled = title.isNotBlank() && items.any { it.isNotBlank() }
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Text(
                    text = "Items",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            itemsIndexed(items) { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = item,
                        onValueChange = { newValue ->
                            items = items.toMutableList().apply {
                                set(index, newValue)
                            }
                        },
                        label = { Text("Item ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    if (items.size > 1) {
                        IconButton(
                            onClick = {
                                items = items.toMutableList().apply {
                                    removeAt(index)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        items = items + ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Item")
                }
            }
        }
    }
}