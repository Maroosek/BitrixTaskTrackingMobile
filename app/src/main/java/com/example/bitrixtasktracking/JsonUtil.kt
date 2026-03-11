package com.example.bitrixtasktracking
import android.content.Context
import java.io.File

class JsonUtil (private val context: Context) {
    private val fileName = "bitrix_tasks_cache.json"

    // Zapisuje surowy JSON, nadpisując stary plik
    fun saveJson(jsonString: String) {
        val file = File(context.filesDir, fileName)
        file.writeText(jsonString)
    }

    // Odczytuje JSON, jeśli istnieje
    fun readJson(): String? {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            file.readText()
        } else {
            null
        }
    }
}