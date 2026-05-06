package com.example.notesy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notesy.data.api.RetrofitInstance
import com.example.notesy.data.repository.NoteRepository
import com.example.notesy.ui.screens.AddNoteScreen
import com.example.notesy.ui.screens.NotesListScreen
import com.example.notesy.ui.theme.NotesyTheme
import com.example.notesy.ui.viewmodel.NotesViewModel
import com.example.notesy.ui.viewmodel.NotesViewModelFactory

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

    val repository = NoteRepository(RetrofitInstance.api)
    val viewModelFactory = NotesViewModelFactory(repository)
    val viewModel: NotesViewModel = viewModel(factory = viewModelFactory)

    NavHost(
        navController = navController,
        startDestination = "notes_list"
    ) {
        composable("notes_list") {
            NotesListScreen(
                viewModel = viewModel,
                onAddNoteClick = {
                    navController.navigate("add_note")
                }
            )
        }

        composable("add_note") {
            AddNoteScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}