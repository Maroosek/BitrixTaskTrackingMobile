package com.example.bitrixtasktracking
import android.content.Context
import java.io.File

class JsonUtil (private val context: Context) {
    private val fileName = "bitrix_tasks_cache.json"

    private val detailsDir = File(context.filesDir, "details").apply {
        if (!exists()) mkdirs() // Tworzy folder, jeśli nie istnieje
    }

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

    fun saveTaskDetail(taskId: String, jsonString: String) {
        val file = File(detailsDir, "task_$taskId.json")
        file.writeText(jsonString)
    }

    fun readTaskDetail(taskId: String): String? {
        val file = File(detailsDir, "task_$taskId.json")
        return if (file.exists()) file.readText() else null
    }

}