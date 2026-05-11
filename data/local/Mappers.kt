package com.example.notesy.data.local

import com.example.notesy.data.model.Folder
import com.example.notesy.data.model.Note
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun Note.toEntity(isSynced: Boolean = true): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        items = Gson().toJson(items),
        folderId = folderId,
        createdAt = createdAt,
        isSynced = isSynced
    )
}

fun NoteEntity.toNote(): Note {
    val type = object : TypeToken<List<String>>() {}.type
    val itemsList: List<String> = Gson().fromJson(items, type) ?: emptyList()

    return Note(
        id = id,
        title = title,
        items = itemsList,
        folderId = folderId,
        createdAt = createdAt
    )
}

fun Folder.toEntity(isSynced: Boolean = true): FolderEntity {
    return FolderEntity(
        id = id,
        name = name,
        createdAt = createdAt,
        isSynced = isSynced
    )
}

fun FolderEntity.toFolder(): Folder {
    return Folder(
        id = id,
        name = name,
        createdAt = createdAt
    )
}