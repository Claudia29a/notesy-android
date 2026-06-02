package com.example.notesy.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
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
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesy.ui.viewmodel.NotesViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val NotesyBg = Color(0xFFF6F3EC)
private val NotesyNavy = Color(0xFF24345D)
private val NotesyGold = Color(0xFFF3BC17)
private val NotesyBlueSheet = Color(0xFFDCE6F4)

private val notesJson = Json {
    ignoreUnknownKeys = true
    prettyPrint = false
}

private enum class BlockType {
    PARAGRAPH,
    BULLET,
    CHECKBOX,
    HEADING1,
    HEADING2,
    HEADING3
}

@Serializable
private data class NoteBlockDto(
    val type: String,
    val text: String,
    val checked: Boolean? = null
)

private data class NoteBlockUi(
    val type: BlockType,
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
    folderId: String? = null,
    onFolderScreenClick: () -> Unit,
    onSettingsClick: () -> Unit
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

    val initialBlocks = remember(existingNote) {
        parseBlocks(existingNote?.content.orEmpty())
    }

    val blocks = remember(existingNote) {
        mutableStateListOf<NoteBlockUi>().apply {
            if (initialBlocks.isEmpty()) {
                add(
                    NoteBlockUi(
                        type = BlockType.PARAGRAPH,
                        value = TextFieldValue("", TextRange(0))
                    )
                )
            } else {
                addAll(initialBlocks)
            }
        }
    }

    var activeBlockIndex by remember { mutableStateOf(0) }
    var pendingFocusIndex by remember { mutableStateOf<Int?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(noteCreated) {
        if (noteCreated) {
            Log.d("AddNoteScreen", "Note saved successfully, navigating back")
            viewModel.resetNoteCreated()
            onNavigateBack()
        }
    }

    fun ensureAtLeastOneBlock() {
        if (blocks.isEmpty()) {
            blocks.add(
                NoteBlockUi(
                    type = BlockType.PARAGRAPH,
                    value = TextFieldValue("", TextRange(0))
                )
            )
            pendingFocusIndex = 0
        }
    }

    fun insertBlockAfter(index: Int, type: BlockType, text: String = "", checked: Boolean? = null) {
        val safeIndex = index.coerceIn(-1, blocks.lastIndex)
        val insertIndex = (safeIndex + 1).coerceAtMost(blocks.size)
        blocks.add(
            insertIndex,
            NoteBlockUi(
                type = type,
                value = TextFieldValue(text, TextRange(text.length)),
                checked = checked
            )
        )
        pendingFocusIndex = insertIndex
    }

    fun updateBlockType(type: BlockType) {
        if (blocks.isEmpty()) return
        val index = activeBlockIndex.coerceIn(0, blocks.lastIndex)
        val current = blocks[index]
        blocks[index] = current.copy(
            type = type,
            checked = if (type == BlockType.CHECKBOX) (current.checked ?: false) else null
        )
        pendingFocusIndex = index
    }

    fun saveCurrentNote() {
        val content = serializeBlocks(blocks)

        if (title.text.isNotBlank()) {
            if (existingNote != null) {
                viewModel.updateNote(
                    existingNote.id,
                    title.text,
                    content,
                    existingNote.folderId
                )
            } else {
                viewModel.createNote(title.text, content, folderId)
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
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = NotesyNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(onClick = { saveCurrentNote() }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = NotesyNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    if (existingNote != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete note",
                                tint = NotesyNavy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    IconButton(onClick = onFolderScreenClick) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Folder",
                            tint = NotesyNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
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
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (title.text.isBlank()) {
                                    Text(
                                        text = "Note title",
                                        color = NotesyNavy.copy(alpha = 0.45f),
                                        fontSize = 22.sp
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

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            IconButton(onClick = {
                                insertBlockAfter(activeBlockIndex, BlockType.BULLET)
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                    contentDescription = "Bullet point",
                                    tint = NotesyNavy,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(onClick = {
                                insertBlockAfter(activeBlockIndex, BlockType.CHECKBOX, checked = false)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.CheckBox,
                                    contentDescription = "Checklist",
                                    tint = NotesyNavy,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            TextButton(
                                onClick = { updateBlockType(BlockType.PARAGRAPH) }
                            ) {
                                Text(
                                    text = "Text",
                                    color = NotesyNavy,
                                    fontSize = 13.sp
                                )
                            }

                            TextButton(
                                onClick = { updateBlockType(BlockType.HEADING1) }
                            ) {
                                Text(
                                    text = "H1",
                                    color = NotesyNavy,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            TextButton(
                                onClick = { updateBlockType(BlockType.HEADING2) }
                            ) {
                                Text(
                                    text = "H2",
                                    color = NotesyNavy,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            TextButton(
                                onClick = { updateBlockType(BlockType.HEADING3) }
                            ) {
                                Text(
                                    text = "H3",
                                    color = NotesyNavy,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                if (existingNote != null) {
                                    viewModel.onSuggestGroceriesClicked(existingNote.id)
                                }
                            }
                            .padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI suggestions",
                            tint = NotesyGold,
                            modifier = Modifier.size(34.dp)
                        )
                        Text(
                            text = "AI suggestions",
                            color = NotesyGold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            itemsIndexed(blocks) { index, block ->
                when (block.type) {
                    BlockType.CHECKBOX -> {
                        EditableCheckboxBlock(
                            block = block,
                            requestFocus = pendingFocusIndex == index,
                            onFocusHandled = {
                                if (pendingFocusIndex == index) pendingFocusIndex = null
                            },
                            onFocused = { activeBlockIndex = index },
                            onToggle = {
                                blocks[index] = block.copy(checked = !(block.checked ?: false))
                            },
                            onValueChange = { newValue ->
                                handleCheckboxValueChange(
                                    blocks = blocks,
                                    index = index,
                                    incoming = newValue,
                                    onInsertNext = { insertIndex, type, text, checked ->
                                        insertBlockAfter(insertIndex, type, text, checked)
                                    }
                                )
                            },
                            onHardwareEnterPressed = {
                                insertBlockAfter(index, BlockType.CHECKBOX, checked = false)
                            },
                            onBackspaceAtEmpty = {
                                if (blocks.size > 1) {
                                    blocks.removeAt(index)
                                    ensureAtLeastOneBlock()
                                    pendingFocusIndex =
                                        (index - 1).coerceAtLeast(0).coerceAtMost(blocks.lastIndex)
                                }
                            }
                        )
                    }

                    else -> {
                        EditableTextBlock(
                            block = block,
                            requestFocus = pendingFocusIndex == index,
                            onFocusHandled = {
                                if (pendingFocusIndex == index) pendingFocusIndex = null
                            },
                            onFocused = { activeBlockIndex = index },
                            onValueChange = { newValue ->
                                handleTextBlockValueChange(
                                    blocks = blocks,
                                    index = index,
                                    incoming = newValue,
                                    onInsertNext = { insertIndex, type, text, checked ->
                                        insertBlockAfter(insertIndex, type, text, checked)
                                    }
                                )
                            },
                            onHardwareEnterPressed = {
                                when (block.type) {
                                    BlockType.BULLET -> insertBlockAfter(index, BlockType.BULLET)
                                    BlockType.HEADING1,
                                    BlockType.HEADING2,
                                    BlockType.HEADING3 -> insertBlockAfter(index, BlockType.PARAGRAPH)
                                    else -> insertBlockAfter(index, BlockType.PARAGRAPH)
                                }
                            },
                            onBackspaceAtEmpty = {
                                if (blocks.size > 1) {
                                    blocks.removeAt(index)
                                    ensureAtLeastOneBlock()
                                    pendingFocusIndex =
                                        (index - 1).coerceAtLeast(0).coerceAtMost(blocks.lastIndex)
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showDeleteDialog && existingNote != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = NotesyBlueSheet,
                shape = RoundedCornerShape(22.dp),
                title = {
                    Text(
                        text = "Delete note?",
                        color = NotesyNavy,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "This action cannot be undone.",
                        color = NotesyNavy
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteNote(existingNote.id)
                            onNavigateBack()
                        }
                    ) {
                        Text(
                            text = "Delete",
                            color = NotesyGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text(
                            text = "Cancel",
                            color = NotesyNavy
                        )
                    }
                }
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
                                itemsIndexed(groceryDialogState.suggestions) { _, suggestion ->
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
private fun EditableTextBlock(
    block: NoteBlockUi,
    requestFocus: Boolean,
    onFocusHandled: () -> Unit,
    onFocused: () -> Unit,
    onValueChange: (TextFieldValue) -> Unit,
    onHardwareEnterPressed: () -> Unit,
    onBackspaceAtEmpty: () -> Unit
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
        verticalAlignment = Alignment.Top
    ) {
        if (block.type == BlockType.BULLET) {
            Text(
                text = "•",
                color = NotesyNavy,
                fontSize = 24.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        BasicTextField(
            value = block.value,
            onValueChange = onValueChange,
            textStyle = blockTextStyle(block.type),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Default
            ),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (it.isFocused) onFocused()
                }
                .onPreviewKeyEvent { event ->
                    when {
                        event.type == KeyEventType.KeyDown && event.key == Key.Enter -> {
                            onHardwareEnterPressed()
                            true
                        }
                        event.type == KeyEventType.KeyDown &&
                                event.key == Key.Backspace &&
                                block.value.text.isEmpty() -> {
                            onBackspaceAtEmpty()
                            true
                        }
                        else -> false
                    }
                },
            decorationBox = { innerTextField ->
                if (block.value.text.isBlank()) {
                    Text(
                        text = blockPlaceholder(block.type),
                        color = NotesyNavy.copy(alpha = 0.35f),
                        fontSize = blockTextStyle(block.type).fontSize
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun EditableCheckboxBlock(
    block: NoteBlockUi,
    requestFocus: Boolean,
    onFocusHandled: () -> Unit,
    onFocused: () -> Unit,
    onToggle: () -> Unit,
    onValueChange: (TextFieldValue) -> Unit,
    onHardwareEnterPressed: () -> Unit,
    onBackspaceAtEmpty: () -> Unit
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
        Checkbox(
            checked = block.checked ?: false,
            onCheckedChange = { onToggle() }
        )

        Spacer(modifier = Modifier.width(6.dp))

        BasicTextField(
            value = block.value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = NotesyNavy,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Default
            ),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (it.isFocused) onFocused()
                }
                .onPreviewKeyEvent { event ->
                    when {
                        event.type == KeyEventType.KeyDown && event.key == Key.Enter -> {
                            onHardwareEnterPressed()
                            true
                        }
                        event.type == KeyEventType.KeyDown &&
                                event.key == Key.Backspace &&
                                block.value.text.isEmpty() -> {
                            onBackspaceAtEmpty()
                            true
                        }
                        else -> false
                    }
                },
            decorationBox = { innerTextField ->
                if (block.value.text.isBlank()) {
                    Text(
                        text = "List item",
                        color = NotesyNavy.copy(alpha = 0.35f),
                        fontSize = 20.sp
                    )
                }
                innerTextField()
            }
        )
    }
}

private fun handleTextBlockValueChange(
    blocks: MutableList<NoteBlockUi>,
    index: Int,
    incoming: TextFieldValue,
    onInsertNext: (Int, BlockType, String, Boolean?) -> Unit
) {
    val newlineIndex = incoming.text.indexOf('\n')
    if (newlineIndex == -1) {
        blocks[index] = blocks[index].copy(value = incoming)
        return
    }

    val before = incoming.text.substring(0, newlineIndex)
    val after = incoming.text.substring(newlineIndex + 1)
    val current = blocks[index]

    blocks[index] = current.copy(
        value = TextFieldValue(before, TextRange(before.length))
    )

    val nextType = when (current.type) {
        BlockType.BULLET -> BlockType.BULLET
        BlockType.HEADING1,
        BlockType.HEADING2,
        BlockType.HEADING3 -> BlockType.PARAGRAPH
        else -> BlockType.PARAGRAPH
    }

    onInsertNext(index, nextType, after, null)
}

private fun handleCheckboxValueChange(
    blocks: MutableList<NoteBlockUi>,
    index: Int,
    incoming: TextFieldValue,
    onInsertNext: (Int, BlockType, String, Boolean?) -> Unit
) {
    val newlineIndex = incoming.text.indexOf('\n')
    if (newlineIndex == -1) {
        blocks[index] = blocks[index].copy(value = incoming)
        return
    }

    val before = incoming.text.substring(0, newlineIndex)
    val after = incoming.text.substring(newlineIndex + 1)
    val current = blocks[index]

    blocks[index] = current.copy(
        value = TextFieldValue(before, TextRange(before.length))
    )

    onInsertNext(index, BlockType.CHECKBOX, after, false)
}

private fun blockTextStyle(type: BlockType): TextStyle {
    return when (type) {
        BlockType.HEADING1 -> TextStyle(
            color = NotesyNavy,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 40.sp
        )
        BlockType.HEADING2 -> TextStyle(
            color = NotesyNavy,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 32.sp
        )
        BlockType.HEADING3 -> TextStyle(
            color = NotesyNavy,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 26.sp
        )
        BlockType.BULLET -> TextStyle(
            color = NotesyNavy,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 24.sp
        )
        else -> TextStyle(
            color = NotesyNavy,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 24.sp
        )
    }
}

private fun blockPlaceholder(type: BlockType): String {
    return when (type) {
        BlockType.HEADING1 -> "Heading 1"
        BlockType.HEADING2 -> "Heading 2"
        BlockType.HEADING3 -> "Heading 3"
        BlockType.BULLET -> "Bullet item"
        BlockType.PARAGRAPH -> "Start typing"
        BlockType.CHECKBOX -> "List item"
    }
}

private fun serializeBlocks(blocks: List<NoteBlockUi>): String {
    val dto = blocks.map {
        NoteBlockDto(
            type = it.type.name,
            text = it.value.text,
            checked = it.checked
        )
    }
    return notesJson.encodeToString(dto)
}

private fun parseBlocks(content: String): List<NoteBlockUi> {
    if (content.isBlank()) return emptyList()

    return try {
        val dto = notesJson.decodeFromString<List<NoteBlockDto>>(content)
        dto.map {
            NoteBlockUi(
                type = it.type.toBlockType(),
                value = TextFieldValue(it.text, TextRange(it.text.length)),
                checked = it.checked
            )
        }
    } catch (_: Exception) {
        parseLegacyPlainText(content)
    }
}

private fun parseLegacyPlainText(content: String): List<NoteBlockUi> {
    if (content.isBlank()) return emptyList()

    return content.lines().map { line ->
        when {
            line.startsWith("☑ ") -> NoteBlockUi(
                type = BlockType.CHECKBOX,
                value = TextFieldValue(line.removePrefix("☑ "), TextRange(line.removePrefix("☑ ").length)),
                checked = true
            )
            line.startsWith("☐ ") -> NoteBlockUi(
                type = BlockType.CHECKBOX,
                value = TextFieldValue(line.removePrefix("☐ "), TextRange(line.removePrefix("☐ ").length)),
                checked = false
            )
            line.startsWith("• ") -> NoteBlockUi(
                type = BlockType.BULLET,
                value = TextFieldValue(line.removePrefix("• "), TextRange(line.removePrefix("• ").length))
            )
            line.startsWith("### ") -> NoteBlockUi(
                type = BlockType.HEADING3,
                value = TextFieldValue(line.removePrefix("### "), TextRange(line.removePrefix("### ").length))
            )
            line.startsWith("## ") -> NoteBlockUi(
                type = BlockType.HEADING2,
                value = TextFieldValue(line.removePrefix("## "), TextRange(line.removePrefix("## ").length))
            )
            line.startsWith("# ") -> NoteBlockUi(
                type = BlockType.HEADING1,
                value = TextFieldValue(line.removePrefix("# "), TextRange(line.removePrefix("# ").length))
            )
            else -> NoteBlockUi(
                type = BlockType.PARAGRAPH,
                value = TextFieldValue(line, TextRange(line.length))
            )
        }
    }
}

private fun String.toBlockType(): BlockType {
    return when (this) {
        "HEADING1" -> BlockType.HEADING1
        "HEADING2" -> BlockType.HEADING2
        "HEADING3" -> BlockType.HEADING3
        "BULLET" -> BlockType.BULLET
        "CHECKBOX" -> BlockType.CHECKBOX
        else -> BlockType.PARAGRAPH
    }
}