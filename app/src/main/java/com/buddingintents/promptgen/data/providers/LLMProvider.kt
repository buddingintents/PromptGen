package com.buddingintents.promptgen.data.providers

import android.util.Log
import com.buddingintents.promptgen.network.ApiClient
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Data class to hold provider configuration
 */
data class ProviderConfig(
    val id: String,
    val displayName: String,
    val baseUrl: String,
    val requiresApiKey: Boolean = true,
    val defaultModel: String = "",
    val description: String = "",
    val supportedFeatures: List<String> = emptyList()
)

/**
 * Data class for API result
 */
data class ApiResult(
    val isSuccessful: Boolean,
    val text: String = "",
    val errorMessage: String? = null,
    val usage: TokenUsage? = null
)

/**
 * Token usage information
 */
data class TokenUsage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0
)

/**
 * Sealed class for different provider types
 */
abstract class LLMProvider(
    val config: ProviderConfig
) {
    abstract suspend fun generateContent(prompt: String, apiKey: String): ApiResult

    protected fun createAuthHeaders(apiKey: String): Map<String, String> {
        return when {
            config.requiresApiKey && apiKey.isNotBlank() -> mapOf(
                "Authorization" to "Bearer $apiKey",
                "Content-Type" to "application/json"
            )
            else -> mapOf("Content-Type" to "application/json")
        }
    }

    protected suspend fun makeApiCall(
        url: String,
        requestBody: String,
        headers: Map<String, String>
    ): ApiResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.post(url, requestBody, headers)

                if (response.isSuccessful) {
                    parseSuccessResponse(response.body)
                } else {
                    val errorMsg = parseErrorResponse(response.body, response.code)
                    Log.w("LLMProvider", "API call failed: $errorMsg")
                    ApiResult(false, errorMessage = errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e("LLMProvider", errorMsg, e)
                FirebaseCrashlytics.getInstance().recordException(e)
                ApiResult(false, errorMessage = errorMsg)
            }
        }
    }

    protected abstract fun parseSuccessResponse(responseBody: String): ApiResult
    protected abstract fun parseErrorResponse(responseBody: String, code: Int): String

    protected fun createSystemPrompt(): String {
        return "You are a prompt engineer. Given the user's brief, create a concise, detailed prompt that the user can paste into an LLM. DO NOT answer the user's request — output only the prompt text."
    }
}