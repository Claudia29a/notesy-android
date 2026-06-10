package com.example.notesy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import kotlinx.serialization.json.Json

private val NotesyBg = Color(0xFFF6F3EC)
private val NotesyNavy = Color(0xFF24345D)
private val NotesyYellow = Color(0xFFF6E79C)
private val NotesyFabBlue = Color(0xFF9CB6E3)
private val NotesyShadowYellow = Color(0xFFE8C84C)
private val NotesySearchBlue = Color(0xFF5373A8)

private val folderNotesJson = Json {
    ignoreUnknownKeys = true
    prettyPrint = false
}

@Serializable
private data class FolderNoteBlockDto(
    val type: String,
    val text: String,
    val checked: Boolean? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderNotesScreen(
    viewModel: NotesViewModel,
    folderId: String?,
    onNavigateBack: () -> Unit,
    onAddNoteClick: () -> Unit,
    onEditNoteClick: (String) -> Unit,
    onFolderClick: (String) -> Unit,
    onFolderScreenClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val folders by viewModel.folders.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val folder = folders.find { it.id == folderId }
    val folderNotes = if (folderId != null) {
        notes.filter { it.folderId == folderId }
    } else {
        notes.filter { it.folderId == null }
    }

    val filteredNotes = folderNotes.filter { note ->
        val searchableContent = extractPlainTextFromContent(note.content)
        note.title.contains(searchQuery, ignoreCase = true) ||
                searchableContent.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = NotesyBg,
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Notesy",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NotesyNavy,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                actions = {
                    Row {
                        IconButton(onClick = onFolderScreenClick) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "Folders",
                                tint = NotesyNavy,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = NotesyNavy,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NotesyBg
                ),
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
                    modifier = Modifier.size(34.dp)
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
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = folder?.name ?: "Notes",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "",
                            color = NotesyNavy.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NotesySearchBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NotesySearchBlue,
                        unfocusedBorderColor = NotesySearchBlue,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = NotesyNavy,
                        focusedTextColor = NotesyNavy,
                        unfocusedTextColor = NotesyNavy
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    folders.forEach { item ->
                        FolderChip(
                            text = item.name,
                            selected = item.id == folderId,
                            onClick = { onFolderClick(item.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (filteredNotes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No notes in this folder yet",
                            color = NotesyNavy.copy(alpha = 0.75f),
                            fontSize = 18.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(filteredNotes) { note ->
                            FolderNoteCard(
                                note = note,
                                onClick = { onEditNoteClick(note.id) }
                            )
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
            .border(
                width = 1.dp,
                color = if (selected) NotesySearchBlue else NotesyNavy.copy(alpha = 0.55f),
                shape = RoundedCornerShape(50.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = Color.Black,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FolderNoteCard(
    note: Note,
    onClick: () -> Unit
) {
    val cleanedContent = extractPlainTextFromContent(note.content)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
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
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = note.title,
                color = Color.Black,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (cleanedContent.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = cleanedContent,
                    color = NotesyNavy.copy(alpha = 0.72f),
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
        val blocks = folderNotesJson.decodeFromString<List<FolderNoteBlockDto>>(content)
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