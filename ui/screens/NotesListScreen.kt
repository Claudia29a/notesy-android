package com.example.notesy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue as runtimeGetValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesy.data.model.Note
import com.example.notesy.ui.viewmodel.NotesViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val notePreviewJson = Json {
    ignoreUnknownKeys = true
    prettyPrint = false
}

@Serializable
private data class NotePreviewBlockDto(
    val type: String,
    val text: String,
    val checked: Boolean? = null
)

private val NotesyBg = Color(0xFFF6F3EC)
private val NotesyNavy = Color(0xFF24345D)
private val NotesyYellow = Color(0xFFF1E39A)
private val NotesyFabBlue = Color(0xFF9CB6E3)
private val NotesyChipBorder = Color(0xFF5A78AF)
private val NotesyShadowYellow = Color(0xFFE8C84C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesViewModel,
    onAddNoteClick: () -> Unit,
    onEditNoteClick: (String) -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFolderId by remember { mutableStateOf<String?>(null) }

    val filteredNotes by remember(notes, searchQuery, selectedFolderId) {
        derivedStateOf {
            notes.filter { note ->
                val matchesFolder = selectedFolderId == null || note.folderId == selectedFolderId
                val matchesQuery =
                    note.title.contains(searchQuery, ignoreCase = true) ||
                            note.content.contains(searchQuery, ignoreCase = true)

                matchesFolder && matchesQuery
            }
        }
    }

    Scaffold(
        containerColor = NotesyBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notesy",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NotesyBg
                ),
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Folders",
                            tint = NotesyNavy
                        )
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNoteClick,
                containerColor = NotesyFabBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Note",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NotesyBg)
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NotesyNavy)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NotesyBg)
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Notes",
                    color = Color.Black,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NotesyChipBorder,
                            modifier = Modifier.size(34.dp)
                        )
                    },
                    placeholder = { Text("") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NotesyChipBorder,
                        unfocusedBorderColor = NotesyChipBorder,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = NotesyNavy,
                        focusedTextColor = NotesyNavy,
                        unfocusedTextColor = NotesyNavy
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(end = 6.dp)
                ) {
                    items(listOf("All") + folders.map { it.name }) { label ->
                        val isSelected = when {
                            label == "All" && selectedFolderId == null -> true
                            else -> folders.find { it.name == label }?.id == selectedFolderId
                        }

                        FolderChip(
                            text = label,
                            selected = isSelected,
                            onClick = {
                                selectedFolderId =
                                    if (label == "All") null
                                    else folders.find { it.name == label }?.id
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                if (filteredNotes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No notes found",
                            color = NotesyNavy.copy(alpha = 0.75f),
                            fontSize = 18.sp
                        )
                    }
                } else {
                    val leftColumn = filteredNotes.filterIndexed { index, _ -> index % 2 == 0 }
                    val rightColumn = filteredNotes.filterIndexed { index, _ -> index % 2 != 0 }

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(18.dp),
                            contentPadding = PaddingValues(bottom = 96.dp)
                        ) {
                            items(leftColumn) { note ->
                                NoteStickyCard(
                                    note = note,
                                    modifier = Modifier.fillMaxWidth(),
                                    height = 150.dp,
                                    onClick = { onEditNoteClick(note.id) }
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(18.dp),
                            contentPadding = PaddingValues(bottom = 96.dp)
                        ) {
                            items(rightColumn) { note ->
                                NoteStickyCard(
                                    note = note,
                                    modifier = Modifier.fillMaxWidth(),
                                    height = 150.dp,
                                    onClick = { onEditNoteClick(note.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(
                color = if (selected) NotesyChipBorder.copy(alpha = 0.08f) else Color.Transparent,
                shape = RoundedCornerShape(40.dp)
            )
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        Card(
            shape = RoundedCornerShape(40.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = NotesyChipBorder
            )
        ) {
            Text(
                text = text,
                color = Color.Black,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
private fun NoteStickyCard(
    note: Note,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val previewText = extractPlainTextFromContent(note.content)

    Card(
        modifier = modifier
            .height(height)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = NotesyShadowYellow
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = NotesyYellow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Text(
                text = note.title,
                color = Color.Black,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (previewText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = previewText,
                    color = NotesyNavy.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun extractPlainTextFromContent(content: String): String {
    if (content.isBlank()) return ""

    return try {
        val blocks = notePreviewJson.decodeFromString<List<NotePreviewBlockDto>>(content)
        blocks.joinToString("\n") { block ->
            when (block.type) {
                "CHECKBOX" -> {
                    val prefix = if (block.checked == true) "☑ " else "☐ "
                    prefix + block.text
                }
                "BULLET" -> "• ${block.text}"
                else -> block.text
            }
        }.trim()
    } catch (_: Exception) {
        content
            .replace("☐", "")
            .replace("☑", "")
            .trim()
    }
}