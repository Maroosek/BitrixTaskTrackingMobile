package com.example.bitrixtasktracking.api

import com.example.bitrixtasktracking.BitrixResponse
import com.example.bitrixtasktracking.SingleTaskResponse
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface BitrixApi {

    @GET("tasks.task.list.json")
    suspend fun getTasks(
        @Query("start") start: Int = 0,
        @Query("filter[REAL_STATUS]") realStatus: Int? = null,
        @Query("filter[>=ACTIVITY_DATE]") activityDate: String? = null
    ): BitrixResponse

    @GET("tasks.task.list.json?start=1100")
    suspend fun getTasksRaw(): ResponseBody

    @GET("tasks.task.list.json?start=1100")
    suspend fun getTasks(): BitrixResponse

    @GET("tasks.task.get")
    suspend fun getTaskDetails(@Query("taskId") taskId: String): SingleTaskResponse
}