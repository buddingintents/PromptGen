package com.buddingintents.promptgen.data.providers

import org.json.JSONArray
import org.json.JSONObject

/**
 * Cohere provider
 */
class CohereProvider(config: ProviderConfig) : LLMProvider(config) {

    override suspend fun generateContent(prompt: String, apiKey: String): ApiResult {
        val requestBody = createRequestBody(prompt)
        val headers = createAuthHeaders(apiKey)

        return makeApiCall(config.baseUrl, requestBody, headers)
    }

    private fun createRequestBody(prompt: String): String {
        val fullPrompt = "${createSystemPrompt()}\n\n$prompt"

        return JSONObject().apply {
            put("model", config.defaultModel)
            put("prompt", fullPrompt)
            put("max_tokens", 1000)
            put("temperature", 0.7)
            put("k", 0)
            put("p", 0.75)
            put("frequency_penalty", 0.0)
            put("presence_penalty", 0.0)
            put("stop_sequences", JSONArray())
            put("return_likelihoods", "NONE")
        }.toString()
    }

    override fun parseSuccessResponse(responseBody: String): ApiResult {
        return try {
            val json = JSONObject(responseBody)

            val text = when {
                json.has("generations") -> {
                    val generations = json.getJSONArray("generations")
                    if (generations.length() > 0) {
                        generations.getJSONObject(0).getString("text").trim()
                    } else {
                        ""
                    }
                }
                json.has("text") -> json.getString("text").trim()
                else -> ""
            }

            if (text.isNotBlank()) {
                ApiResult(true, text)
            } else {
                ApiResult(false, errorMessage = "Empty response from Cohere")
            }
        } catch (e: Exception) {
            ApiResult(false, errorMessage = "Failed to parse Cohere response: ${e.message}")
        }
    }

    override fun parseErrorResponse(responseBody: String, code: Int): String {
        return try {
            val json = JSONObject(responseBody)
            val message = json.optString("message", "Unknown Cohere error")
            "Cohere Error ($code): $message"
        } catch (e: Exception) {
            "Cohere HTTP Error: $code - $responseBody"
        }
    }
}