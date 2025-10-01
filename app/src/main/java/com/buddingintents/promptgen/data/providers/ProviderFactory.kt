package com.buddingintents.promptgen.data.providers

/**
 * Provider factory to create appropriate provider instances
 */
object ProviderFactory {

    private val providers = mapOf(
        "openai" to ProviderConfig(
            id = "openai",
            displayName = "OpenAI",
            baseUrl = "https://api.openai.com/v1/chat/completions",
            requiresApiKey = true,
            defaultModel = "gpt-4o",
            description = "OpenAI GPT models",
            supportedFeatures = listOf("chat", "completion", "streaming")
        ),
        "gemini" to ProviderConfig(
            id = "gemini",
            displayName = "Google Gemini (free)",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent",
            requiresApiKey = true,
            defaultModel = "gemini-2.0-flash-exp",
            description = "Google's Gemini AI model - Free tier available",
            supportedFeatures = listOf("chat", "completion", "safety_settings")
        ),
        "cohere" to ProviderConfig(
            id = "cohere",
            displayName = "Cohere (free)",
            baseUrl = "https://api.cohere.ai/v1/generate",
            requiresApiKey = true,
            defaultModel = "command-r",
            description = "Cohere language models - Free tier available",
            supportedFeatures = listOf("completion", "embeddings")
        ),
        "huggingface" to ProviderConfig(
            id = "huggingface",
            displayName = "Hugging Face (community)",
            baseUrl = "https://api-inference.huggingface.co/models/microsoft/DialoGPT-large",
            requiresApiKey = false,
            defaultModel = "microsoft/DialoGPT-large",
            description = "Hugging Face Inference API - Community models",
            supportedFeatures = listOf("completion", "inference")
        ),
        "perplexity" to ProviderConfig(
            id = "perplexity",
            displayName = "Perplexity",
            baseUrl = "https://api.perplexity.ai/chat/completions",
            requiresApiKey = true,
            defaultModel = "sonar",
            description = "Perplexity search-enabled AI",
            supportedFeatures = listOf("chat", "search", "real_time")
        ),
        "openrouter" to ProviderConfig(
            id = "openrouter",
            displayName = "OpenRouter (free/credits)",
            baseUrl = "https://openrouter.ai/api/v1/chat/completions",
            requiresApiKey = true,
            defaultModel = "meta-llama/llama-3.1-8b-instruct:free",
            description = "OpenRouter model aggregator - Some free models",
            supportedFeatures = listOf("chat", "completion", "multiple_models")
        ),
        "ollama" to ProviderConfig(
            id = "ollama",
            displayName = "On-Device (local)",
            baseUrl = "http://127.0.0.1:11434/api/generate",
            requiresApiKey = false,
            defaultModel = "llama3.2",
            description = "Local Ollama instance - Privacy focused",
            supportedFeatures = listOf("local", "offline", "privacy")
        ),
        "custom" to ProviderConfig(
            id = "custom",
            displayName = "Custom",
            baseUrl = "",
            requiresApiKey = true,
            defaultModel = "",
            description = "Custom API endpoint",
            supportedFeatures = listOf("custom")
        )
    )

    /**
     * Create provider instance by ID
     */
    fun createProvider(providerId: String): LLMProvider? {
        val config = providers[providerId] ?: return null

        return when (providerId) {
            "openai", "perplexity", "openrouter" -> OpenAIProvider(config)
            "gemini" -> GeminiProvider(config)
            "cohere" -> CohereProvider(config)
            "huggingface" -> HuggingFaceProvider(config)
            "ollama" -> OllamaProvider(config)
            "custom" -> OpenAIProvider(config) // Default to OpenAI-compatible for custom
            else -> null
        }
    }

    /**
     * Get all available provider configurations
     */
    fun getAllProviders(): List<ProviderConfig> = providers.values.toList()

    /**
     * Get provider config by ID
     */
    fun getProviderConfig(providerId: String): ProviderConfig? = providers[providerId]

    /**
     * Get provider display names for UI
     */
    fun getProviderNames(): List<String> = providers.values.map { it.displayName }

    /**
     * Find provider ID by display name
     */
    fun findProviderIdByName(displayName: String): String? {
        return providers.entries.find { it.value.displayName == displayName }?.key
    }
}