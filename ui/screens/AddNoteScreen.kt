package com.example.notesy.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesy.ui.viewmodel.NotesViewModel

private val NotesyBg = Color(0xFFF6F3EC)
private val NotesyNavy = Color(0xFF24345D)
private val NotesyGold = Color(0xFFF3BC17)
private val NotesyBlueSheet = Color(0xFFDCE6F4)

private data class NoteLineUi(
    val id: Long,
    val value: TextFieldValue,
    val checked: Boolean? = null
)

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
    val folders by viewModel.folders.collectAsState()
    val noteCreated by viewModel.noteCreated.collectAsState()
    val groceryDialogState by viewModel.groceryDialogState.collectAsState()

    val existingNote = noteId?.let { id -> notes.find { it.id == id } }
    val folderName = folders.find { it.id == (existingNote?.folderId ?: folderId) }?.name ?: "Notes"

    var title by remember(existingNote) {
        mutableStateOf(TextFieldValue(existingNote?.title ?: ""))
    }

    val parsedLines = remember(existingNote) {
        parseNoteLines(existingNote?.content ?: "")
    }

    val noteLines = remember(existingNote) {
        mutableStateListOf<NoteLineUi>().apply { addAll(parsedLines) }
    }

    var nextLineId by remember(existingNote) {
        mutableLongStateOf((parsedLines.maxOfOrNull { it.id } ?: 0L) + 1L)
    }

    var activeLineId by remember { mutableStateOf<Long?>(null) }
    var pendingFocusLineId by remember { mutableStateOf<Long?>(null) }
    val contentScrollState = rememberScrollState()

    LaunchedEffect(noteCreated) {
        if (noteCreated) {
            Log.d("AddNoteScreen", "Note saved successfully, navigating back")
            viewModel.resetNoteCreated()
            onNavigateBack()
        }
    }

    fun saveCurrentNote() {
        val mergedContent = buildString {
            noteLines.forEachIndexed { index, line ->
                when (line.checked) {
                    true -> append("☑ ${line.value.text}")
                    false -> append("☐ ${line.value.text}")
                    null -> append(line.value.text)
                }
                if (index != noteLines.lastIndex) append("\n")
            }
        }.trimEnd()

        if (title.text.isNotBlank()) {
            if (existingNote != null) {
                viewModel.updateNote(
                    existingNote.id,
                    title.text,
                    mergedContent,
                    existingNote.folderId
                )
            } else {
                viewModel.createNote(title.text, mergedContent, folderId)
            }
        }
    }

    fun insertLineAfterActive(checked: Boolean?, prefix: String = "") {
        val newLine = NoteLineUi(
            id = nextLineId,
            value = TextFieldValue(prefix, TextRange(prefix.length)),
            checked = checked
        )
        nextLineId += 1L

        val activeIndex = noteLines.indexOfFirst { it.id == activeLineId }
        if (activeIndex >= 0) {
            noteLines.add(activeIndex + 1, newLine)
        } else {
            noteLines.add(newLine)
        }

        pendingFocusLineId = newLine.id
    }

    fun toggleBoldAtActiveLine() {
        val activeIndex = noteLines.indexOfFirst { it.id == activeLineId }
        if (activeIndex >= 0) {
            val current = noteLines[activeIndex]
            val text = current.value.text
            val selection = current.value.selection
            val start = selection.start.coerceAtLeast(0)
            val end = selection.end.coerceAtLeast(start)

            val updated = if (start != end) {
                val newText =
                    text.substring(0, start) +
                            "**" +
                            text.substring(start, end) +
                            "**" +
                            text.substring(end)
                TextFieldValue(newText, TextRange(end + 4))
            } else {
                val newText =
                    text.substring(0, start) + "****" + text.substring(end)
                TextFieldValue(newText, TextRange(start + 2))
            }

            noteLines[activeIndex] = current.copy(value = updated)
            pendingFocusLineId = current.id
        } else {
            val text = title.text
            val selection = title.selection
            val start = selection.start.coerceAtLeast(0)
            val end = selection.end.coerceAtLeast(start)

            title = if (start != end) {
                val newText =
                    text.substring(0, start) +
                            "**" +
                            text.substring(start, end) +
                            "**" +
                            text.substring(end)
                TextFieldValue(newText, TextRange(end + 4))
            } else {
                val newText = text.substring(0, start) + "****" + text.substring(end)
                TextFieldValue(newText, TextRange(start + 2))
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
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NotesyNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = NotesyNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Folder",
                            tint = NotesyNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { saveCurrentNote() }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Save",
                            tint = NotesyNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NotesyBg
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(NotesyBg)
                .padding(padding)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
                .padding(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        BasicTextField(
                            value = title,
                            onValueChange = { title = it },
                            textStyle = TextStyle(
                                color = NotesyNavy,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (title.text.isBlank()) {
                                    Text(
                                        text = "Note title",
                                        color = NotesyNavy.copy(alpha = 0.45f),
                                        fontSize = 28.sp
                                    )
                                }
                                innerTextField()
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Note in $folderName folder",
                            color = NotesyNavy.copy(alpha = 0.88f),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

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
                containerColor = NotesyBlueSheet,
                shape = RoundedCornerShape(22.dp),
                title = {
                    Text(
                        text = "AI suggestions",
                        color = NotesyNavy,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    if (groceryDialogState.suggestions.isEmpty()) {
                        Text(
                            text = "No suggestions found.",
                            color = NotesyNavy
                        )
                    } else {
                        Column {
                            TextButton(
                                onClick = { viewModel.onSelectAllSuggestedItems() }
                            ) {
                                Text("Select all", color = NotesyGold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier.heightIn(max = 320.dp)
                            ) {
                                items(groceryDialogState.suggestions.size) { index ->
                                    val suggestion = groceryDialogState.suggestions[index]

                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                text = suggestion.itemName,
                                                color = NotesyNavy
                                            )
                                        },
                                        supportingContent = {
                                            Text(
                                                text = suggestion.storeName,
                                                color = NotesyNavy.copy(alpha = 0.8f)
                                            )
                                        },
                                        leadingContent = {
                                            Checkbox(
                                                checked = groceryDialogState.selectedKeys.contains(suggestion.key),
                                                onCheckedChange = {
                                                    viewModel.onToggleSuggestedItem(suggestion.key)
                                                }
                                            )
                                        }
                                    )

                                    HorizontalDivider(
                                        color = NotesyNavy.copy(alpha = 0.15f)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.addSelectedSuggestedItems(existingNote.id) }
                    ) {
                        Text("Add", color = NotesyGold, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.dismissGrocerySuggestions() }
                    ) {
                        Text("Cancel", color = NotesyNavy)
                    }
                }
            )
        }
    }
}

@Composable
private fun EditablePlainLine(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onFocused: () -> Unit,
    textStyle: TextStyle
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (it.isFocused) onFocused()
            },
        decorationBox = { innerTextField -> innerTextField() }
    )
}

@Composable
private fun EditableChecklistRow(
    item: NoteLineUi,
    requestFocus: Boolean,
    onFocusHandled: () -> Unit,
    onFocused: () -> Unit,
    onToggle: () -> Unit,
    onTextChange: (TextFieldValue) -> Unit,
    onEnterPressed: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            focusRequester.requestFocus()
            onFocusHandled()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (item.checked == true) "☑" else "☐",
            color = NotesyNavy,
            fontSize = 30.sp,
            modifier = Modifier.clickable { onToggle() }
        )

        Spacer(modifier = Modifier.width(10.dp))

        BasicTextField(
            value = item.value,
            onValueChange = onTextChange,
            textStyle = TextStyle(
                color = NotesyNavy,
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (it.isFocused) onFocused()
                }
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                        onEnterPressed()
                        true
                    } else {
                        false
                    }
                },
            decorationBox = { innerTextField -> innerTextField() }
        )
    }
}

private fun parseNoteLines(content: String): List<NoteLineUi> {
    if (content.isBlank()) return emptyList()

    var nextId = 1L
    return content.lines().map { line ->
        when {
            line.startsWith("☑ ") -> NoteLineUi(
                id = nextId++,
                value = TextFieldValue(
                    text = line.removePrefix("☑ "),
                    selection = TextRange(line.removePrefix("☑ ").length)
                ),
                checked = true
            )

            line.startsWith("☐ ") -> NoteLineUi(
                id = nextId++,
                value = TextFieldValue(
                    text = line.removePrefix("☐ "),
                    selection = TextRange(line.removePrefix("☐ ").length)
                ),
                checked = false
            )

            else -> NoteLineUi(
                id = nextId++,
                value = TextFieldValue(
                    text = line,
                    selection = TextRange(line.length)
                ),
                checked = null
            )
        }
    }
}

private fun isSectionHeader(text: String): Boolean {
    return text.trim().matches(Regex("^Week\\s+\\d+$", RegexOption.IGNORE_CASE))
}