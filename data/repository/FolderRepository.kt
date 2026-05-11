package com.example.notesy.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.notesy.data.api.NoteApiService
import com.example.notesy.data.local.FolderDao
import com.example.notesy.data.local.FolderEntity
import com.example.notesy.data.local.toEntity
import com.example.notesy.data.local.toFolder
import com.example.notesy.data.model.CreateFolderRequest
import com.example.notesy.data.model.Folder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID

class FolderRepository(
    private val apiService: NoteApiService,
    private val folderDao: FolderDao
) {

    fun getAllFoldersFlow(): Flow<List<Folder>> {
        return folderDao.getAllFolders().map { entities ->
            entities.map { it.toFolder() }
        }
    }

    suspend fun syncWithBackend() {
        try {
            val unsyncedFolders = folderDao.getUnsyncedFolders()

            unsyncedFolders.forEach { localFolder ->
                try {
                    val createdFolder = apiService.createFolder(
                        CreateFolderRequest(name = localFolder.name)
                    )

                    folderDao.deleteFolderById(localFolder.id)
                    folderDao.insertFolder(createdFolder.toEntity(isSynced = true))
                } catch (_: Exception) {
                }
            }

            val foldersFromApi = apiService.getFolders()
            val entities = foldersFromApi.map { it.toEntity(isSynced = true) }
            folderDao.insertFolders(entities)
        } catch (_: Exception) {
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createFolder(name: String) {
        val localFolder = FolderEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            createdAt = Instant.now().toString(),
            isSynced = false
        )

        folderDao.insertFolder(localFolder)

        try {
            val createdFolder = apiService.createFolder(
                CreateFolderRequest(name = name)
            )

            folderDao.deleteFolderById(localFolder.id)
            folderDao.insertFolder(createdFolder.toEntity(isSynced = true))
        } catch (_: Exception) {
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateFolder(id: String, name: String) {
        val existingFolder = folderDao.getFolderById(id)
        val updatedFolder = FolderEntity(
            id = id,
            name = name,
            createdAt = existingFolder?.createdAt ?: Instant.now().toString(),
            isSynced = false
        )

        folderDao.insertFolder(updatedFolder)

        try {
            val serverFolder = apiService.updateFolder(
                id = id,
                request = CreateFolderRequest(name = name)
            )

            folderDao.insertFolder(serverFolder.toEntity(isSynced = true))
        } catch (_: Exception) {
        }
    }

    suspend fun deleteFolder(id: String) {
        folderDao.deleteFolderById(id)

        try {
            apiService.deleteFolder(id)
        } catch (_: Exception) {
        }
    }
}