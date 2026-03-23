package com.example.notesappcompose.network

import com.example.notesappcompose.data.remote.TopStoriesResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NyTimesApi {
    @GET("home.json")
    suspend fun getHomeTopStories(@Query("api-key") apiKey: String): TopStoriesResponseDto
}
