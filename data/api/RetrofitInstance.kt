package com.example.notesy.data.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

object RetrofitInstance {
    private const val BASE_URL = "https://notesy-backend-tjj8.onrender.com/"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val api: NoteApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(NoteApiService::class.java)
    }
}