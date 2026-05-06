package com.example.notesy.data.api

import com.example.notesy.data.model.GroceryNote
import com.example.notesy.data.model.PreferredItem
import retrofit2.Response
import retrofit2.http.*

interface NotesApi {

    // Notes endpoints
    @GET("notes")
    suspend fun getAllNotes(): List<GroceryNote>

    @GET("notes/{id}")
    suspend fun getNoteById(@Path("id") id: String): GroceryNote

    @POST("notes")
    suspend fun createNote(@Body note: GroceryNote): GroceryNote

    @PUT("notes/{id}")
    suspend fun updateNote(@Path("id") id: String, @Body note: GroceryNote): GroceryNote

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: String): Response<String>

    // Preferred items endpoints
    @GET("preferred-items")
    suspend fun getAllPreferredItems(): List<PreferredItem>

    @POST("preferred-items")
    suspend fun addPreferredItem(@Body item: PreferredItem): Response<PreferredItem>

    @DELETE("preferred-items/{id}")
    suspend fun deletePreferredItem(@Path("id") id: String): Response<String>
}