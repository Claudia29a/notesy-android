package com.example.notesy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notesy.ui.screens.AddNoteScreen
import com.example.notesy.ui.screens.FolderNotesScreen
import com.example.notesy.ui.screens.FoldersListScreen
import com.example.notesy.ui.theme.NotesyTheme
import com.example.notesy.ui.viewmodel.NotesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesyTheme {
                NotesyApp()
            }
        }
    }
}

@Composable
fun NotesyApp() {
    val navController = rememberNavController()
    val viewModel: NotesViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "folders_list"
    ) {
        composable("folders_list") {
            FoldersListScreen(
                viewModel = viewModel,
                onFolderClick = { folderId ->
                    navController.navigate("folder_notes/$folderId")
                },
                onViewAllNotes = {
                    navController.navigate("folder_notes/null")
                }
            )
        }

        composable("folder_notes/{folderId}") { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId")
            val actualFolderId = if (folderId == "null") null else folderId

            FolderNotesScreen(
                viewModel = viewModel,
                folderId = actualFolderId,
                onNavigateBack = { navController.popBackStack() },
                onAddNoteClick = {
                    navController.navigate("add_note/$folderId")
                },
                onEditNoteClick = { noteId ->
                    navController.navigate("edit_note/$noteId")
                }
            )
        }

        composable("add_note/{folderId}") { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId")
            val actualFolderId = if (folderId == "null") null else folderId

            AddNoteScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                noteId = null,
                folderId = actualFolderId
            )
        }

        composable("edit_note/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")

            AddNoteScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                noteId = noteId,
                folderId = null
            )
        }
    }
}