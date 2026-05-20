package com.example.notesy.data.api

import com.example.notesy.data.model.AiSuggestRequest
import com.example.notesy.data.model.AiSuggestResponse
import com.example.notesy.data.model.CreateFolderRequest
import com.example.notesy.data.model.CreateNoteRequest
import com.example.notesy.data.model.Folder
import com.example.notesy.data.model.Note
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface NoteApiService {
    @GET("folders")
    suspend fun getFolders(): List<Folder>

    @POST("folders")
    suspend fun createFolder(@Body request: CreateFolderRequest): Folder

    @PUT("folders/{id}")
    suspend fun updateFolder(
        @Path("id") id: String,
        @Body request: CreateFolderRequest
    ): Folder

    @DELETE("folders/{id}")
    suspend fun deleteFolder(@Path("id") id: String)

    @GET("notes")
    suspend fun getNotes(): List<Note>

    @GET("folders/{folderId}/notes")
    suspend fun getNotesByFolder(@Path("folderId") folderId: String): List<Note>

    @POST("notes")
    suspend fun createNote(@Body request: CreateNoteRequest): Note

    @PUT("notes/{id}")
    suspend fun updateNote(
        @Path("id") id: String,
        @Body request: CreateNoteRequest
    ): Note

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: String)

    @POST("ai/suggest-groceries")
    suspend fun suggestGroceries(@Body request: AiSuggestRequest): AiSuggestResponse
}