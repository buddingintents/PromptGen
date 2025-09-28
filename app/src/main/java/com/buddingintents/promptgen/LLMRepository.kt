
package com.buddingintents.promptgen

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType

data class ApiResult(val isSuccessful: Boolean, val text: String = "", val errorMessage: String? = null)

class LLMRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("PromptAppPrefs", Context.MODE_PRIVATE)
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    fun generateRefinedPrompt(userInput: String, theme: String): ApiResult {
        val provider = prefs.getString("provider", "OpenAI (preset)") ?: "OpenAI (preset)"
        val apiKey = prefs.getString("apikey", "") ?: ""
        var endpoint = prefs.getString("endpoint", "") ?: ""

        if (endpoint.isBlank()) {
            endpoint = when (provider) {
                "OpenAI (preset)" -> "https://api.openai.com/v1/chat/completions"
                "HuggingFace (preset)" -> "https://api-inference.huggingface.co/models/gpt2"
                "Cohere (preset)" -> "https://api.cohere.ai/generate"
                "Perplexity" -> "https://api.perplexity.ai/chat/completions"
                else -> ""
            }
        }

        if (endpoint.isBlank()) {
            return ApiResult(false, errorMessage = "No endpoint configured. Open Settings.")
        }

        val system = "You are a prompt engineer. Given the user's brief, create a concise, detailed prompt that the user can paste into an LLM. DO NOT answer the user's request — output only the prompt text."
        val userMsg = "Theme: " + theme + "\nUser: " + userInput + "\nDeliver: A single prompt (no sample answer)."
        val json = buildPayloadForEndpoint(endpoint, system, userMsg)

        val requestBuilder = Request.Builder()
            .url(endpoint)
            .post(RequestBody.create("application/json; charset=utf-8".toMediaType(), json))

        if (apiKey.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer " + apiKey)
        }

        requestBuilder.addHeader("Accept", "application/json")

        val request = requestBuilder.build()

        try {
            client.newCall(request).execute().use { resp ->
                val body = resp.body?.string() ?: ""
                if (!resp.isSuccessful) {
                    return ApiResult(false, errorMessage = "HTTP ${resp.code}: $body")
                }
                val parsed = extractTextFromResponse(body)
                return ApiResult(true, text = parsed)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().log(e.toString())
            return ApiResult(false, errorMessage = e.message)
        }
    }

    private fun buildPayloadForEndpoint(endpoint: String, system: String, user: String): String {
        return when {
            endpoint.contains("openai.com") -> {
                val m = JSONObject()
                m.put("model", "gpt-4o")
                val messages = org.json.JSONArray()
                val sm = JSONObject(); sm.put("role", "system"); sm.put("content", system)
                val um = JSONObject(); um.put("role", "user"); um.put("content", user)
                messages.put(sm); messages.put(um)
                m.put("messages", messages)
                m.toString()
            }
            endpoint.contains("huggingface.co") -> {
                val m = JSONObject()
                m.put("inputs", user)
                m.toString()
            }
            endpoint.contains("cohere") -> {
                val m = JSONObject()
                m.put("model", "command-xlarge-nightly")
                m.put("prompt", user)
                m.put("max_tokens", 300)
                m.toString()
            }
            // After (Correct)
            endpoint.contains("perplexity") -> {
                val m = JSONObject()
                m.put("model", "sonar-pro") // Using a recommended model
                val messages = org.json.JSONArray()
                val sm = JSONObject(); sm.put("role", "system"); sm.put("content", system)
                val um = JSONObject(); um.put("role", "user"); um.put("content", user)
                messages.put(sm);
                messages.put(um)
                m.put("messages", messages)
                m.toString()
            }
            else -> {
                val m = JSONObject()
                m.put("input", user)
                m.toString()
            }
        }
    }

    private fun extractTextFromResponse(body: String): String {
        try {
            val j = JSONObject(body)
            if (j.has("choices")) {
                val choices = j.getJSONArray("choices")
                if (choices.length() > 0) {
                    val first = choices.getJSONObject(0)
                    if (first.has("message")) {
                        val msg = first.getJSONObject("message")
                        return msg.optString("content", "")
                    } else if (first.has("text")) {
                        return first.optString("text", "")
                    }
                }
            }
            if (j.has("generations")) {
                val gens = j.getJSONArray("generations")
                if (gens.length() > 0) return gens.getJSONObject(0).optString("text", "")
            }
            if (j.has("generated_text")) {
                return j.optString("generated_text", "")
            }
            Log.d("TAG", "response format: $body");
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().log(e.toString())
            Log.e("TAG", "An error occurred:", e);
        }
        return body
    }
}
