package com.buddingintents.promptgen.data.providers

import org.json.JSONArray
import org.json.JSONObject

/**
 * OpenAI-compatible provider (OpenAI, Perplexity, OpenRouter)
 */
class OpenAIProvider(config: ProviderConfig) : LLMProvider(config) {

    override suspend fun generateContent(prompt: String, apiKey: String): ApiResult {
        val requestBody = createRequestBody(prompt)
        val headers = createAuthHeaders(apiKey)

        return makeApiCall(config.baseUrl, requestBody, headers)
    }

    private fun createRequestBody(prompt: String): String {
        return JSONObject().apply {
            put("model", config.defaultModel)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", createSystemPrompt())
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("max_tokens", 1000)
            put("temperature", 0.7)
            put("top_p", 1.0)
        }.toString()
    }

    override fun parseSuccessResponse(responseBody: String): ApiResult {
        return try {
            val json = JSONObject(responseBody)
            val choices = json.getJSONArray("choices")

            if (choices.length() > 0) {
                val message = choices.getJSONObject(0).getJSONObject("message")
                val content = message.getString("content").trim()

                // Parse usage if available
                val usage = if (json.has("usage")) {
                    val usageObj = json.getJSONObject("usage")
                    TokenUsage(
                        promptTokens = usageObj.optInt("prompt_tokens", 0),
                        completionTokens = usageObj.optInt("completion_tokens", 0),
                        totalTokens = usageObj.optInt("total_tokens", 0)
                    )
                } else null

                ApiResult(true, content, usage = usage)
            } else {
                ApiResult(false, errorMessage = "No choices in response")
            }
        } catch (e: Exception) {
            ApiResult(false, errorMessage = "Failed to parse response: ${e.message}")
        }
    }

    override fun parseErrorResponse(responseBody: String, code: Int): String {
        return try {
            val json = JSONObject(responseBody)
            val error = json.optJSONObject("error")
            if (error != null) {
                val message = error.optString("message", "Unknown error")
                val type = error.optString("type", "")
                "API Error ($code): $type - $message"
            } else {
                "HTTP Error: $code"
            }
        } catch (e: Exception) {
            "HTTP Error: $code - $responseBody"
        }
    }
}