package com.example.notesy.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.notesy.data.api.RetrofitInstance
import com.example.notesy.data.local.NotesDatabase
import com.example.notesy.data.repository.NoteRepository

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = NotesDatabase.getDatabase(applicationContext)
            val repository = NoteRepository(RetrofitInstance.api, database.noteDao())
            repository.syncWithBackend()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}