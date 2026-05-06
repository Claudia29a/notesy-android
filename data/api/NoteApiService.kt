package com.example.notesy.data.api

import com.example.notesy.data.model.CreateNoteRequest
import com.example.notesy.data.model.Note
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NoteApiService {
    @GET("notes")
    suspend fun getNotes(): List<Note>

    @POST("notes")
    suspend fun createNote(@Body request: CreateNoteRequest): Note

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: String)
}