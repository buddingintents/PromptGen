package com.buddingintents.promptgen.data

import android.content.Context
import com.buddingintents.promptgen.data.providers.CohereProvider
import com.buddingintents.promptgen.data.providers.GeminiProvider
import com.buddingintents.promptgen.data.providers.LLMProvider
import com.buddingintents.promptgen.data.providers.ProviderFactory
import com.buddingintents.promptgen.data.providers.OpenAIProvider
import com.buddingintents.promptgen.data.providers.HuggingFaceProvider
import com.buddingintents.promptgen.data.providers.OllamaProvider
import org.json.JSONArray
import org.json.JSONObject

/**
 * Data class for storing provider credentials
 */
data class ProviderCredential(
    val providerId: String,
    val apiKey: String,
    val customEndpoint: String = "",
    val isActive: Boolean = false,
    val customModel: String = ""
)

/**
 * Storage manager for provider configurations and API keys
 */
class ProviderConfigStorage(private val context: Context) {
    companion object {
        private const val PREFS_NAME = "PromptAppPrefs"
        private const val KEY_PROVIDER_CONFIGS = "provider_configurations"
        private const val KEY_ACTIVE_PROVIDER = "active_provider"
        private const val KEY_MIGRATION_DONE = "migration_v2_done"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        // Migrate from old single-key storage to new multi-key storage
        migrateFromOldStorage()
    }

    /**
     * Save provider credential configuration
     */
    fun saveProviderCredential(credential: ProviderCredential) {
        val existingConfigs = getStoredConfigurations().toMutableList()

        // Remove existing config for this provider
        existingConfigs.removeAll { it.providerId == credential.providerId }

        // If setting as active, deactivate all others
        if (credential.isActive) {
            existingConfigs.forEach { config ->
                val index = existingConfigs.indexOf(config)
                existingConfigs[index] = config.copy(isActive = false)
            }
            setActiveProvider(credential.providerId)
        }

        existingConfigs.add(credential)
        saveConfigurations(existingConfigs)
    }

    /**
     * Get API key for specific provider
     */
    fun getApiKey(providerId: String): String {
        return getStoredConfigurations()
            .find { it.providerId == providerId }
            ?.apiKey ?: ""
    }

    /**
     * Get custom endpoint for provider
     */
    fun getCustomEndpoint(providerId: String): String {
        return getStoredConfigurations()
            .find { it.providerId == providerId }
            ?.customEndpoint ?: ""
    }

    /**
     * Get custom model for provider
     */
    fun getCustomModel(providerId: String): String {
        return getStoredConfigurations()
            .find { it.providerId == providerId }
            ?.customModel ?: ""
    }

    /**
     * Get all configured providers
     */
    fun getAllConfiguredProviders(): List<ProviderCredential> {
        return getStoredConfigurations()
    }

    /**
     * Get active provider instance
     */
    fun getActiveProvider(): LLMProvider {
        val activeProviderId = getActiveProviderId()
        var provider = ProviderFactory.createProvider(activeProviderId)
            ?: ProviderFactory.createProvider("gemini")!!

        // Apply customizations if configured
        val credential = getStoredConfigurations().find { it.providerId == activeProviderId }
        if (credential != null) {
            var updatedConfig = provider.config

            // Apply custom endpoint
            if (credential.customEndpoint.isNotBlank()) {
                updatedConfig = updatedConfig.copy(baseUrl = credential.customEndpoint)
            }

            // Apply custom model
            if (credential.customModel.isNotBlank()) {
                updatedConfig = updatedConfig.copy(defaultModel = credential.customModel)
            }

            // Recreate provider with updated config if changes were made
            if (updatedConfig != provider.config) {
                provider = when (activeProviderId) {
                    "openai", "perplexity", "openrouter", "custom" -> OpenAIProvider(updatedConfig)
                    "gemini" -> GeminiProvider(updatedConfig)
                    "cohere" -> CohereProvider(updatedConfig)
                    "huggingface" -> HuggingFaceProvider(updatedConfig)
                    "ollama" -> OllamaProvider(updatedConfig)
                    else -> provider
                }
            }
        }

        return provider
    }

    /**
     * Set active provider
     */
    fun setActiveProvider(providerId: String) {
        prefs.edit().putString(KEY_ACTIVE_PROVIDER, providerId).apply()

        // Update stored configurations to reflect active status
        val configs = getStoredConfigurations().map { config ->
            config.copy(isActive = config.providerId == providerId)
        }
        saveConfigurations(configs)
    }

    /**
     * Remove provider configuration
     */
    fun removeProviderConfig(providerId: String) {
        val updatedConfigs = getStoredConfigurations().filter { it.providerId != providerId }
        saveConfigurations(updatedConfigs)

        // If removing active provider, set first available as active
        if (getActiveProviderId() == providerId && updatedConfigs.isNotEmpty()) {
            setActiveProvider(updatedConfigs.first().providerId)
        }
    }

    /**
     * Check if provider has valid configuration
     */
    fun isProviderConfigured(providerId: String): Boolean {
        val config = ProviderFactory.getProviderConfig(providerId) ?: return false
        val apiKey = getApiKey(providerId)

        return if (config.requiresApiKey) {
            apiKey.isNotBlank()
        } else {
            true // Providers that don't require API keys are always "configured"
        }
    }

    /**
     * Get the current active provider ID
     */
    fun getActiveProviderId(): String {
        return prefs.getString(KEY_ACTIVE_PROVIDER, "gemini") ?: "gemini"
    }

    /**
     * Get provider credential by ID
     */
    fun getProviderCredential(providerId: String): ProviderCredential? {
        return getStoredConfigurations().find { it.providerId == providerId }
    }

    private fun getStoredConfigurations(): List<ProviderCredential> {
        val configJson = prefs.getString(KEY_PROVIDER_CONFIGS, "[]") ?: "[]"
        val configArray = try { JSONArray(configJson) } catch (e: Exception) { JSONArray() }

        val configurations = mutableListOf<ProviderCredential>()
        for (i in 0 until configArray.length()) {
            try {
                val configObj = configArray.getJSONObject(i)
                configurations.add(
                    ProviderCredential(
                        providerId = configObj.getString("providerId"),
                        apiKey = configObj.optString("apiKey", ""),
                        customEndpoint = configObj.optString("customEndpoint", ""),
                        isActive = configObj.optBoolean("isActive", false),
                        customModel = configObj.optString("customModel", "")
                    )
                )
            } catch (e: Exception) {
                // Skip malformed entries
            }
        }

        return configurations
    }

    private fun saveConfigurations(configurations: List<ProviderCredential>) {
        val configArray = JSONArray()
        configurations.forEach { config ->
            val configObj = JSONObject().apply {
                put("providerId", config.providerId)
                put("apiKey", config.apiKey)
                put("customEndpoint", config.customEndpoint)
                put("isActive", config.isActive)
                put("customModel", config.customModel)
            }
            configArray.put(configObj)
        }

        prefs.edit()
            .putString(KEY_PROVIDER_CONFIGS, configArray.toString())
            .apply()
    }

    /**
     * Migrate from old single-key storage to new multi-key storage
     */
    private fun migrateFromOldStorage() {
        if (prefs.getBoolean(KEY_MIGRATION_DONE, false)) {
            return // Migration already completed
        }

        // Get old values
        val oldProvider = prefs.getString("provider", null)
        val oldApiKey = prefs.getString("apikey", null)
        val oldEndpoint = prefs.getString("endpoint", null)

        if (oldProvider != null && oldApiKey != null) {
            // Convert old provider name to new provider ID
            val providerId = when (oldProvider) {
                "Google Gemini (free)" -> "gemini"
                "Cohere (free)" -> "cohere"
                "Hugging Face (community)" -> "huggingface"
                "OpenRouter (free/credits)" -> "openrouter"
                "On-Device (local)" -> "ollama"
                "OpenAI (preset)" -> "openai"
                "HuggingFace (preset)" -> "huggingface"
                "Cohere (preset)" -> "cohere"
                "Perplexity" -> "perplexity"
                "Custom" -> "custom"
                else -> "gemini"
            }

            // Create credential from old data
            val credential = ProviderCredential(
                providerId = providerId,
                apiKey = oldApiKey,
                customEndpoint = oldEndpoint ?: "",
                isActive = true
            )

            // Save the migrated credential
            saveProviderCredential(credential)

            // Clean up old keys
            prefs.edit()
                .remove("provider")
                .remove("apikey")
                .remove("endpoint")
                .putBoolean(KEY_MIGRATION_DONE, true)
                .apply()
        } else {
            // No old data to migrate
            prefs.edit().putBoolean(KEY_MIGRATION_DONE, true).apply()
        }
    }
}