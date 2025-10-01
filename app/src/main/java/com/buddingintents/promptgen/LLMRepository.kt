package com.buddingintents.promptgen

import android.content.Context
import com.buddingintents.promptgen.data.ProviderConfigStorage
import com.buddingintents.promptgen.data.providers.ApiResult
import com.buddingintents.promptgen.data.providers.LLMProvider

/**
 * Repository for handling LLM API calls
 * Simplified version that delegates to the new provider architecture
 */
class LLMRepository(private val context: Context) {
    private val configStorage = ProviderConfigStorage(context)

    /**
     * Generate refined prompt using the active provider
     */
    suspend fun generateRefinedPrompt(userInput: String, theme: String): ApiResult {
        val activeProvider = configStorage.getActiveProvider()
        val apiKey = configStorage.getApiKey(activeProvider.config.id)

        if (activeProvider.config.requiresApiKey && apiKey.isBlank()) {
            return ApiResult(
                false,
                errorMessage = "API key required for ${activeProvider.config.displayName}. Please configure in settings."
            )
        }

        val fullPrompt = buildPrompt(userInput, theme)
        return activeProvider.generateContent(fullPrompt, apiKey)
    }

    /**
     * Get the currently active provider
     */
    fun getActiveProvider(): LLMProvider {
        return configStorage.getActiveProvider()
    }

    /**
     * Check if the current provider is properly configured
     */
    fun isProviderConfigured(): Boolean {
        val activeProviderId = configStorage.getActiveProviderId()
        return configStorage.isProviderConfigured(activeProviderId)
    }

    /**
     * Get provider configuration storage for settings
     */
    fun getConfigStorage(): ProviderConfigStorage {
        return configStorage
    }

    private fun buildPrompt(userInput: String, theme: String): String {
        return buildString {
            appendLine("Theme: $theme")
            appendLine("User request: $userInput")
            appendLine()
            appendLine("Instructions: Create a concise, detailed prompt that the user can paste into an LLM.")
            appendLine("DO NOT answer the user's request — output only the prompt text.")
            appendLine("Make the prompt specific, actionable, and well-structured.")
        }
    }
}