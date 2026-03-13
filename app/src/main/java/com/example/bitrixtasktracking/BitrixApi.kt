package com.example.bitrixtasktracking.api

import android.app.Dialog
import com.example.bitrixtasktracking.BitrixResponse
import com.example.bitrixtasktracking.ChatResponse
import com.example.bitrixtasktracking.SingleTaskResponse
import com.example.bitrixtasktracking.UsersResponse
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BitrixApi {

    @GET("tasks.task.list.json")
    suspend fun getTasks(
        @Query("start") start: Int = 0,
        @Query("filter[REAL_STATUS]") realStatus: Int? = null,
        @Query("filter[>=ACTIVITY_DATE]") activityDate: String? = null
    ): BitrixResponse

//    @GET("tasks.task.list.json?start=1100")
//    suspend fun getTasksRaw(): ResponseBody

//    @GET("tasks.task.get")
//    suspend fun getTaskDetails(@Query("taskId") taskId: String): SingleTaskResponse

    @GET("im.dialog.messages.get")
    suspend fun getChatMessages(
        @Query("DIALOG_ID") dialogId: String,
        @Query("LIMIT") limit: Int = 50,
        @Query("LAST_ID") lastId: Int? = null
    ): ChatResponse

    @GET("tasks.task.get")
    suspend fun getTaskDetailsRaw(@Query("taskId") taskId: String): ResponseBody

    @POST("im.message.add")
    suspend fun sendMessage(
        @Query("DIALOG_ID") chatId: String,
        @Query("MESSAGE") message: String,
        @Query("SYSTEM") system: String,
    )

    @GET("tasks.task.list.json")
    suspend fun getTasksForUser(
        @Query("start") start: Int = 0,
        // Grupa filtrów z logiką OR (użytkownik w dowolnej roli)
        @Query("filter[0][::LOGIC]") logic: String = "OR",
        @Query("filter[0][CREATED_BY]") creatorId: String,
        @Query("filter[0][RESPONSIBLE_ID]") responsibleId: String,
        @Query("filter[0][ACCOMPLICE]") accompliceId: String,
        // Filtr daty poza grupą [0], co domyślnie łączy się operatorem AND
        @Query("filter[>=ACTIVITY_DATE]") activityDate: String? = null
    ): BitrixResponse

    @GET("user.get.json")
    suspend fun getUsers(
        @Query("ACTIVE") active: Boolean,
        @Query("start") start: Int = 0): UsersResponse
}