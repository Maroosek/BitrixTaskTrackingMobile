package com.example.bitrixtasktracking.network

import com.example.bitrixtasktracking.Config
import com.example.bitrixtasktracking.api.BitrixApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val api: BitrixApi by lazy {
        Retrofit.Builder()
            .baseUrl(Config.webhookUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BitrixApi::class.java)
    }
}