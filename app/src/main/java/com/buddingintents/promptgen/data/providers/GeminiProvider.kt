package com.buddingintents.promptgen.data.providers

import org.json.JSONArray
import org.json.JSONObject

/**
 * Google Gemini provider
 */
class GeminiProvider(config: ProviderConfig) : LLMProvider(config) {

    override suspend fun generateContent(prompt: String, apiKey: String): ApiResult {
        val url = "${config.baseUrl}?key=$apiKey"
        val requestBody = createRequestBody(prompt)
        val headers = mapOf("Content-Type" to "application/json")

        return makeApiCall(url, requestBody, headers)
    }

    private fun createRequestBody(prompt: String): String {
        return JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "${createSystemPrompt()}\n\n$prompt"))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("topP", 1.0)
                put("maxOutputTokens", 1000)
                put("candidateCount", 1)
            })
            put("safetySettings", JSONArray().apply {
                val categories = listOf(
                    "HARM_CATEGORY_HARASSMENT",
                    "HARM_CATEGORY_HATE_SPEECH",
                    "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                    "HARM_CATEGORY_DANGEROUS_CONTENT"
                )
                categories.forEach { category ->
                    put(JSONObject().apply {
                        put("category", category)
                        put("threshold", "BLOCK_MEDIUM_AND_ABOVE")
                    })
                }
            })
        }.toString()
    }

    override fun parseSuccessResponse(responseBody: String): ApiResult {
        return try {
            val json = JSONObject(responseBody)
            val candidates = json.getJSONArray("candidates")

            if (candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")

                if (parts.length() > 0) {
                    val text = parts.getJSONObject(0).getString("text").trim()
                    ApiResult(true, text)
                } else {
                    ApiResult(false, errorMessage = "No text parts in response")
                }
            } else {
                ApiResult(false, errorMessage = "No candidates in response")
            }
        } catch (e: Exception) {
            ApiResult(false, errorMessage = "Failed to parse Gemini response: ${e.message}")
        }
    }

    override fun parseErrorResponse(responseBody: String, code: Int): String {
        return try {
            val json = JSONObject(responseBody)
            val error = json.optJSONObject("error")
            if (error != null) {
                val message = error.optString("message", "Unknown Gemini error")
                val status = error.optString("status", "")
                "Gemini Error ($code): $status - $message"
            } else {
                "Gemini HTTP Error: $code"
            }
        } catch (e: Exception) {
            "Gemini HTTP Error: $code - $responseBody"
        }
    }
}