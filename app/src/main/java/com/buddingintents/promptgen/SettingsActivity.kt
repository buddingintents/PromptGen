package com.buddingintents.promptgen

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buddingintents.promptgen.data.ProviderConfigStorage
import com.buddingintents.promptgen.data.ProviderCredential
import com.buddingintents.promptgen.data.providers.ProviderFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog

class SettingsActivity : AppCompatActivity() {

    private lateinit var spinnerProvider: Spinner
    private lateinit var etApiKey: EditText
    private lateinit var etEndpoint: EditText
    private lateinit var etCustomModel: EditText
    private lateinit var tvNotes: TextView
    private lateinit var btnSave: Button
    private lateinit var btnSetActive: Button
    private lateinit var btnDelete: Button
    private lateinit var rvConfiguredProviders: RecyclerView
    private lateinit var tvActiveProvider: TextView

    private lateinit var configStorage: ProviderConfigStorage
    private lateinit var providerAdapter: ProviderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        configStorage = ProviderConfigStorage(this)

        initializeViews()
        setupProviderList()
        setupClickListeners()
        updateActiveProviderDisplay()

        // Load current provider if editing
        val activeProviderId = configStorage.getActiveProviderId()
        loadProviderConfiguration(activeProviderId)
    }

    private fun initializeViews() {
        spinnerProvider = findViewById(R.id.spinnerProvider)
        etApiKey = findViewById(R.id.etApiKey)
        etEndpoint = findViewById(R.id.etEndpoint)
        etCustomModel = findViewById(R.id.etCustomModel)
        tvNotes = findViewById(R.id.tvProviderNotes)
        btnSave = findViewById(R.id.btnSave)
        btnSetActive = findViewById(R.id.btnSetActive)
        btnDelete = findViewById(R.id.btnDelete)
        rvConfiguredProviders = findViewById(R.id.rvConfiguredProviders)
        tvActiveProvider = findViewById(R.id.tvActiveProvider)

        // Setup spinner with all available providers
        val providers = ProviderFactory.getProviderNames()
        spinnerProvider.adapter = ArrayAdapter(
            this,
            R.layout.item_spinner_text,
            R.id.tvSpinnerText,
            providers
        ).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown_text)
        }

        spinnerProvider.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                val providerName = providers[position]
                val providerId = ProviderFactory.findProviderIdByName(providerName)
                if (providerId != null) {
                    configureUIForProvider(providerId)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupProviderList() {
        providerAdapter = ProviderAdapter(
            onProviderClick = { credential ->
                loadProviderConfiguration(credential.providerId)
            },
            onSetActiveClick = { credential ->
                configStorage.setActiveProvider(credential.providerId)
                updateActiveProviderDisplay()
                providerAdapter.updateData(configStorage.getAllConfiguredProviders())
                Toast.makeText(this, "${ProviderFactory.getProviderConfig(credential.providerId)?.displayName} is now active", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { credential ->
                showDeleteConfirmation(credential)
            }
        )

        rvConfiguredProviders.layoutManager = LinearLayoutManager(this)
        rvConfiguredProviders.adapter = providerAdapter

        providerAdapter.updateData(configStorage.getAllConfiguredProviders())
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            saveCurrentConfiguration()
        }

        btnSetActive.setOnClickListener {
            val selectedProviderName = spinnerProvider.selectedItem.toString()
            val providerId = ProviderFactory.findProviderIdByName(selectedProviderName)
            if (providerId != null) {
                configStorage.setActiveProvider(providerId)
                updateActiveProviderDisplay()
                providerAdapter.updateData(configStorage.getAllConfiguredProviders())
                Toast.makeText(this, "$selectedProviderName is now active", Toast.LENGTH_SHORT).show()
            }
        }

        btnDelete.setOnClickListener {
            val selectedProviderName = spinnerProvider.selectedItem.toString()
            val providerId = ProviderFactory.findProviderIdByName(selectedProviderName)
            if (providerId != null) {
                val credential = configStorage.getProviderCredential(providerId)
                if (credential != null) {
                    showDeleteConfirmation(credential)
                }
            }
        }
    }

    private fun loadProviderConfiguration(providerId: String) {
        val config = ProviderFactory.getProviderConfig(providerId) ?: return
        val credential = configStorage.getProviderCredential(providerId)

        // Set spinner selection
        val providerNames = ProviderFactory.getProviderNames()
        val index = providerNames.indexOf(config.displayName)
        if (index >= 0) {
            spinnerProvider.setSelection(index)
        }

        // Load stored values
        if (credential != null) {
            etApiKey.setText(credential.apiKey)
            etEndpoint.setText(credential.customEndpoint.ifBlank { config.baseUrl })
            etCustomModel.setText(credential.customModel.ifBlank { config.defaultModel })
        } else {
            etApiKey.setText("")
            etEndpoint.setText(config.baseUrl)
            etCustomModel.setText(config.defaultModel)
        }

        configureUIForProvider(providerId)
    }

    private fun configureUIForProvider(providerId: String) {
        val config = ProviderFactory.getProviderConfig(providerId) ?: return

        // Configure API key field
        if (config.requiresApiKey) {
            etApiKey.visibility = View.VISIBLE
            etApiKey.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            etApiKey.hint = "Enter your API key"
        } else {
            etApiKey.visibility = View.GONE
        }

        // Always show endpoint and model fields for customization
        etEndpoint.visibility = View.VISIBLE
        etCustomModel.visibility = View.VISIBLE

        // Set provider-specific notes and defaults
        when (providerId) {
            "gemini" -> {
                tvNotes.text = "Google Gemini AI - Free tier available via Google AI Studio. Get your API key from aistudio.google.com"
                if (etEndpoint.text.isNullOrBlank()) {
                    etEndpoint.setText(config.baseUrl)
                }
            }
            "cohere" -> {
                tvNotes.text = "Cohere language models - Free tier available. Get your API key from cohere.ai"
                if (etEndpoint.text.isNullOrBlank()) {
                    etEndpoint.setText(config.baseUrl)
                }
            }
            "huggingface" -> {
                tvNotes.text = "Hugging Face Inference API - Many community models available. API token optional but recommended."
                if (etEndpoint.text.isNullOrBlank()) {
                    etEndpoint.setText(config.baseUrl)
                }
            }
            "openrouter" -> {
                tvNotes.text = "OpenRouter - Access to multiple AI models. Some free models available with credits. Get key from openrouter.ai"
                if (etEndpoint.text.isNullOrBlank()) {
                    etEndpoint.setText(config.baseUrl)
                }
            }
            "ollama" -> {
                tvNotes.text = "Local Ollama instance - No API key needed. Make sure Ollama is running locally on port 11434."
                if (etEndpoint.text.isNullOrBlank()) {
                    etEndpoint.setText(config.baseUrl)
                }
            }
            "openai" -> {
                tvNotes.text = "OpenAI GPT models - Requires paid API key from platform.openai.com"
                if (etEndpoint.text.isNullOrBlank()) {
                    etEndpoint.setText(config.baseUrl)
                }
            }
            "perplexity" -> {
                tvNotes.text = "Perplexity AI - Search-enhanced responses. Get API key from perplexity.ai/settings/api"
                if (etEndpoint.text.isNullOrBlank()) {
                    etEndpoint.setText(config.baseUrl)
                }
            }
            "custom" -> {
                tvNotes.text = "Custom API endpoint - Configure your own OpenAI-compatible API. Adjust endpoint and model as needed."
            }
        }

        // Update button visibility
        val isConfigured = configStorage.isProviderConfigured(providerId)
        btnDelete.visibility = if (isConfigured) View.VISIBLE else View.GONE
    }

    private fun saveCurrentConfiguration() {
        val selectedProviderName = spinnerProvider.selectedItem.toString()
        val providerId = ProviderFactory.findProviderIdByName(selectedProviderName) ?: return
        val config = ProviderFactory.getProviderConfig(providerId) ?: return

        val apiKey = etApiKey.text.toString().trim()
        val endpoint = etEndpoint.text.toString().trim()
        val customModel = etCustomModel.text.toString().trim()

        // Validate required fields
        if (config.requiresApiKey && apiKey.isBlank()) {
            Toast.makeText(this, "API key is required for ${config.displayName}", Toast.LENGTH_SHORT).show()
            return
        }

        // Create and save credential
        val credential = ProviderCredential(
            providerId = providerId,
            apiKey = apiKey,
            customEndpoint = if (endpoint != config.baseUrl) endpoint else "",
            customModel = if (customModel != config.defaultModel) customModel else "",
            isActive = false // Will be set if user clicks "Set Active"
        )

        configStorage.saveProviderCredential(credential)
        providerAdapter.updateData(configStorage.getAllConfiguredProviders())

        Toast.makeText(this, "Configuration saved for ${config.displayName}", Toast.LENGTH_SHORT).show()
    }

    private fun updateActiveProviderDisplay() {
        val activeProvider = configStorage.getActiveProvider()
        tvActiveProvider.text = "Active Provider: ${activeProvider.config.displayName}"
    }

    private fun showDeleteConfirmation(credential: ProviderCredential) {
        val config = ProviderFactory.getProviderConfig(credential.providerId)
        AlertDialog.Builder(this)
            .setTitle("Delete Configuration")
            .setMessage("Delete saved configuration for ${config?.displayName}?")
            .setPositiveButton("Delete") { _, _ ->
                configStorage.removeProviderConfig(credential.providerId)
                providerAdapter.updateData(configStorage.getAllConfiguredProviders())
                updateActiveProviderDisplay()
                Toast.makeText(this, "Configuration deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class ProviderAdapter(
    private val onProviderClick: (ProviderCredential) -> Unit,
    private val onSetActiveClick: (ProviderCredential) -> Unit,
    private val onDeleteClick: (ProviderCredential) -> Unit
) : RecyclerView.Adapter<ProviderAdapter.ViewHolder>() {

    private var providers = listOf<ProviderCredential>()

    fun updateData(newProviders: List<ProviderCredential>) {
        providers = newProviders
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_provider_config, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(providers[position])
    }

    override fun getItemCount() = providers.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvProviderName: TextView = view.findViewById(R.id.tvProviderName)
        private val tvProviderStatus: TextView = view.findViewById(R.id.tvProviderStatus)
        private val btnSetActive: Button = view.findViewById(R.id.btnSetActive)
        private val btnDelete: Button = view.findViewById(R.id.btnDelete)
        private val ivActiveIndicator: View = view.findViewById(R.id.ivActiveIndicator)

        fun bind(credential: ProviderCredential) {
            val config = ProviderFactory.getProviderConfig(credential.providerId)

            tvProviderName.text = config?.displayName ?: credential.providerId
            tvProviderStatus.text = if (credential.apiKey.isNotBlank()) "Configured" else "No API key"

            ivActiveIndicator.visibility = if (credential.isActive) View.VISIBLE else View.GONE
            btnSetActive.isEnabled = !credential.isActive
            btnSetActive.text = if (credential.isActive) "Active" else "Set Active"

            itemView.setOnClickListener { onProviderClick(credential) }
            btnSetActive.setOnClickListener { onSetActiveClick(credential) }
            btnDelete.setOnClickListener { onDeleteClick(credential) }
        }
    }
}