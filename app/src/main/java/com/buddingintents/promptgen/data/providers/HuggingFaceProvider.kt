package com.buddingintents.promptgen.data.providers

import org.json.JSONArray
import org.json.JSONObject

/**
 * HuggingFace provider
 */
class HuggingFaceProvider(config: ProviderConfig) : LLMProvider(config) {

    override suspend fun generateContent(prompt: String, apiKey: String): ApiResult {
        val requestBody = createRequestBody(prompt)
        val headers = if (apiKey.isNotBlank()) {
            createAuthHeaders(apiKey)
        } else {
            mapOf("Content-Type" to "application/json")
        }

        return makeApiCall(config.baseUrl, requestBody, headers)
    }

    private fun createRequestBody(prompt: String): String {
        val fullPrompt = "${createSystemPrompt()}\n\nUser: $prompt\nAssistant:"

        return JSONObject().apply {
            put("inputs", fullPrompt)
            put("parameters", JSONObject().apply {
                put("max_new_tokens", 1000)
                put("temperature", 0.7)
                put("top_p", 0.95)
                put("do_sample", true)
                put("return_full_text", false)
            })
            put("options", JSONObject().apply {
                put("wait_for_model", true)
            })
        }.toString()
    }

    override fun parseSuccessResponse(responseBody: String): ApiResult {
        return try {
            val text = when {
                responseBody.trim().startsWith("[") -> {
                    val jsonArray = JSONArray(responseBody)
                    if (jsonArray.length() > 0) {
                        val firstResult = jsonArray.getJSONObject(0)
                        firstResult.optString("generated_text", "").trim()
                    } else {
                        ""
                    }
                }
                responseBody.trim().startsWith("{") -> {
                    val json = JSONObject(responseBody)
                    json.optString("generated_text", responseBody).trim()
                }
                else -> responseBody.trim()
            }

            if (text.isNotBlank()) {
                ApiResult(true, text)
            } else {
                ApiResult(false, errorMessage = "Empty response from HuggingFace")
            }
        } catch (e: Exception) {
            ApiResult(false, errorMessage = "Failed to parse HuggingFace response: ${e.message}")
        }
    }

    override fun parseErrorResponse(responseBody: String, code: Int): String {
        return try {
            val json = JSONObject(responseBody)
            val error = json.optString("error", "Unknown HuggingFace error")
            "HuggingFace Error ($code): $error"
        } catch (e: Exception) {
            "HuggingFace HTTP Error: $code - $responseBody"
        }
    }
}