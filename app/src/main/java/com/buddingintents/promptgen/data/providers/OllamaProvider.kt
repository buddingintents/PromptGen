package com.buddingintents.promptgen.data.providers

import org.json.JSONObject

/**
 * Ollama (Local) provider
 */
class OllamaProvider(config: ProviderConfig) : LLMProvider(config) {

    override suspend fun generateContent(prompt: String, apiKey: String): ApiResult {
        val requestBody = createRequestBody(prompt)
        val headers = mapOf("Content-Type" to "application/json")

        return makeApiCall(config.baseUrl, requestBody, headers)
    }

    private fun createRequestBody(prompt: String): String {
        val fullPrompt = "${createSystemPrompt()}\n\n$prompt"

        return JSONObject().apply {
            put("model", config.defaultModel)
            put("prompt", fullPrompt)
            put("stream", false)
            put("options", JSONObject().apply {
                put("temperature", 0.7)
                put("top_p", 0.9)
                put("num_predict", 1000)
            })
        }.toString()
    }

    override fun parseSuccessResponse(responseBody: String): ApiResult {
        return try {
            val json = JSONObject(responseBody)
            val response = json.optString("response", "").trim()

            if (response.isNotBlank()) {
                ApiResult(true, response)
            } else {
                ApiResult(false, errorMessage = "Empty response from Ollama")
            }
        } catch (e: Exception) {
            ApiResult(false, errorMessage = "Failed to parse Ollama response: ${e.message}")
        }
    }

    override fun parseErrorResponse(responseBody: String, code: Int): String {
        return try {
            val json = JSONObject(responseBody)
            val error = json.optString("error", "Unknown Ollama error")
            "Ollama Error ($code): $error"
        } catch (e: Exception) {
            "Ollama HTTP Error: $code - Check if Ollama is running on localhost:11434"
        }
    }
}