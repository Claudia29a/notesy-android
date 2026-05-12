package com.example.notesy.data.local

import com.example.notesy.data.model.Folder
import com.example.notesy.data.model.Note

fun Note.toEntity(isSynced: Boolean = true): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,  // Changed from items and removed JSON conversion
        folderId = folderId,
        createdAt = createdAt,
        isSynced = isSynced
    )
}

fun NoteEntity.toNote(): Note {
    return Note(
        id = id,
        title = title,
        content = content,  // Changed from items and removed JSON conversion
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