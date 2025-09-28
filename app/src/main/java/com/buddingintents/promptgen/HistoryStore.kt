package com.buddingintents.promptgen

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject


data class PromptRecord(val userText: String, val generatedPrompt: String)


object HistoryStore {
    private const val PREFS = "PromptAppPrefs"
    private const val KEY_HISTORY = "history_records"
    private const val MAX_SIZE = 50

    fun addRecord(context: Context, record: PromptRecord) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        // Read existing JSON array (or empty if none)
        val currentJson = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        val existing = try { JSONArray(currentJson) } catch (e: Exception) { JSONArray() }

        // New record
        val obj = JSONObject().apply {
            put("user", record.userText)
            put("prompt", record.generatedPrompt)
        }

        // Prepend new record and keep only the last MAX_SIZE
        val newArr = JSONArray()
        newArr.put(obj)
        val limit = minOf(existing.length(), MAX_SIZE - 1)
        for (idx in 0 until limit) {
            newArr.put(existing.getJSONObject(idx))
        }

        prefs.edit().putString(KEY_HISTORY, newArr.toString()).apply()
    }

    fun getRecords(context: Context): List<PromptRecord> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        val arr = try { JSONArray(json) } catch (e: Exception) { JSONArray() }

        val list = mutableListOf<PromptRecord>()
        for (idx in 0 until arr.length()) {
            val o = arr.getJSONObject(idx)
            list.add(
                PromptRecord(
                    o.optString("user", ""),
                    o.optString("prompt", "")
                )
            )
        }
        return list
    }
}