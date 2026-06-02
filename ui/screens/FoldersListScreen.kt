package com.example.notesy.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesy.data.model.Folder
import com.example.notesy.ui.viewmodel.NotesViewModel

private val NotesyBg = Color(0xFFF6F3EC)
private val NotesyNavy = Color(0xFF24345D)
private val NotesyYellow = Color(0xFFF1E39A)
private val NotesyFabBlue = Color(0xFF9CB6E3)
private val NotesyShadowYellow = Color(0xFFE8C84C)
private val NotesyDeleteRed = Color(0xFFB34747)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersListScreen(
    viewModel: NotesViewModel,
    onFolderClick: (String) -> Unit,
    onViewAllNotes: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val folders by viewModel.folders.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var folderToDelete by remember { mutableStateOf<Folder?>(null) }

    val filteredFolders = folders.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = NotesyBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notesy",
                            color = Color.Black,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

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
                onClick = { showCreateDialog = true },
                containerColor = NotesyFabBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Folder",
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
                    text = "Folders",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(18.dp))

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
                            tint = Color(0xFF5373A8),
                            modifier = Modifier.size(34.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5373A8),
                        unfocusedBorderColor = Color(0xFF5373A8),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = NotesyNavy,
                        focusedTextColor = NotesyNavy,
                        unfocusedTextColor = NotesyNavy
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                if (filteredFolders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (folders.isEmpty()) {
                                "No folders yet"
                            } else {
                                "No matching folders"
                            },
                            color = NotesyNavy.copy(alpha = 0.75f),
                            fontSize = 18.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(filteredFolders) { folder ->
                            FolderPillCard(
                                folder = folder,
                                noteCount = notes.count { it.folderId == folder.id },
                                onClick = { onFolderClick(folder.id) },
                                onDeleteClick = { folderToDelete = folder }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createFolder(name)
                showCreateDialog = false
            }
        )
    }

    if (folderToDelete != null) {
        DeleteFolderDialog(
            folderName = folderToDelete!!.name,
            onDismiss = { folderToDelete = null },
            onConfirmDelete = {
                viewModel.deleteFolder(folderToDelete!!.id)
                folderToDelete = null
            }
        )
    }
}

@Composable
private fun FolderPillCard(
    folder: Folder,
    noteCount: Int,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = NotesyShadowYellow
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = NotesyYellow
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete folder",
                    tint = NotesyDeleteRed
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = folder.name,
                    color = Color.Black,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (noteCount > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$noteCount notes",
                        color = NotesyNavy.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NotesyBg,
        title = {
            Text(
                text = "Create Folder",
                color = NotesyNavy,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("Folder Name", color = NotesyNavy) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NotesyNavy,
                    unfocusedBorderColor = NotesyNavy.copy(alpha = 0.65f),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = NotesyNavy,
                    focusedTextColor = NotesyNavy,
                    unfocusedTextColor = NotesyNavy
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(folderName.trim()) },
                enabled = folderName.isNotBlank()
            ) {
                Text(
                    text = "Create",
                    color = NotesyNavy,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = NotesyNavy
                )
            }
        }
    )
}

@Composable
private fun DeleteFolderDialog(
    folderName: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NotesyBg,
        title = {
            Text(
                text = "Delete Folder",
                color = NotesyDeleteRed,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$folderName\"?",
                color = NotesyNavy
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmDelete) {
                Text(
                    text = "Delete",
                    color = NotesyDeleteRed,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = NotesyNavy
                )
            }
        }
    )
}