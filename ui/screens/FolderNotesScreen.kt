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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

private val NotesyBg = Color(0xFFF6F3EC)
private val NotesyNavy = Color(0xFF24345D)
private val NotesyYellow = Color(0xFFF1E39A)
private val NotesyFabBlue = Color(0xFF9CB6E3)
private val NotesyShadowYellow = Color(0xFFE8C84C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderNotesScreen(
    viewModel: NotesViewModel,
    folderId: String?,
    onNavigateBack: () -> Unit,
    onAddNoteClick: () -> Unit,
    onEditNoteClick: (String) -> Unit
) {
    val folders by viewModel.folders.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val folder = folders.find { it.id == folderId }
    val folderNotes = if (folderId != null) {
        notes.filter { it.folderId == folderId }
    } else {
        notes.filter { it.folderId == null }
    }

    Scaffold(
        containerColor = NotesyBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = folder?.name ?: "Notes",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NotesyNavy
                        )
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
        } else if (folderNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NotesyBg)
                    .padding(padding),
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
                modifier = Modifier
                    .fillMaxSize()
                    .background(NotesyBg)
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
            ) {
                items(folderNotes) { note ->
                    FolderNoteCard(
                        note = note,
                        onClick = { onEditNoteClick(note.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FolderNoteCard(
    note: Note,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = NotesyShadowYellow
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = NotesyYellow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = note.title,
                color = Color.Black,
                fontSize = 21.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = note.content
                        .replace("☐", "")
                        .replace("☑", "")
                        .trim(),
                    color = NotesyNavy.copy(alpha = 0.78f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = note.createdAt,
                    color = NotesyNavy.copy(alpha = 0.55f),
                    fontSize = 11.sp
                )
            }
        }
    }
}