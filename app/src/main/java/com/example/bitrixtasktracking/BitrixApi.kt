package com.example.bitrixtasktracking.api

import okhttp3.ResponseBody
import retrofit2.http.GET

interface BitrixApi {

    @GET("tasks.task.list.json")
    suspend fun getTasks(): ResponseBody

}