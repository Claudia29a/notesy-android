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
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(listOf("")) }

    // Observe note creation state
    val noteCreated by viewModel.noteCreated.collectAsState()

    // Navigate back when note is created
    LaunchedEffect(noteCreated) {
        if (noteCreated) {
            Log.d("AddNoteScreen", "✅ Note created successfully, navigating back")
            viewModel.resetNoteCreated()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Note") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            Log.d("AddNoteScreen", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                            Log.d("AddNoteScreen", "🔵 SAVE BUTTON CLICKED")
                            Log.d("AddNoteScreen", "Title: '$title'")
                            Log.d("AddNoteScreen", "Total items: ${items.size}")
                            Log.d("AddNoteScreen", "Items: $items")

                            if (title.isNotBlank() && items.any { it.isNotBlank() }) {
                                val validItems = items.filter { it.isNotBlank() }
                                Log.d("AddNoteScreen", "✅ Validation passed")
                                Log.d("AddNoteScreen", "Valid items count: ${validItems.size}")
                                Log.d("AddNoteScreen", "Valid items: $validItems")
                                Log.d("AddNoteScreen", "🚀 Calling viewModel.createNote()")
                                viewModel.createNote(title, validItems)
                            } else {
                                Log.w("AddNoteScreen", "❌ Validation FAILED")
                                Log.w("AddNoteScreen", "Title blank? ${title.isBlank()}")
                                Log.w("AddNoteScreen", "No valid items? ${!items.any { it.isNotBlank() }}")
                            }
                            Log.d("AddNoteScreen", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
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