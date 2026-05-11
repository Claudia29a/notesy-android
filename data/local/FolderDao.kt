package com.example.notesy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders ORDER BY createdAt DESC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :id LIMIT 1")
    suspend fun getFolderById(id: String): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<FolderEntity>): List<Long>

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteFolderById(id: String): Int

    @Query("SELECT * FROM folders WHERE isSynced = 0")
    suspend fun getUnsyncedFolders(): List<FolderEntity>

    @Query("DELETE FROM folders")
    suspend fun deleteAllFolders(): Int
}