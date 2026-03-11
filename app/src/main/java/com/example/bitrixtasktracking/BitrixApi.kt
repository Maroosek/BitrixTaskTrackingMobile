package com.example.bitrixtasktracking.api

import com.example.bitrixtasktracking.BitrixResponse
import okhttp3.ResponseBody
import retrofit2.http.GET

interface BitrixApi {

    @GET("tasks.task.list.json?start=1100")
    suspend fun getTasksRaw(): ResponseBody


    @GET("tasks.task.list.json?start=1100")
    suspend fun getTasks(): BitrixResponse
}