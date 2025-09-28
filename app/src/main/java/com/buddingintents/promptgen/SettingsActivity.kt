package com.buddingintents.promptgen

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private val PREFS = "PromptAppPrefs"
    private val KEY_PROVIDER = "provider"
    private val KEY_APIKEY = "apikey"
    private val KEY_ENDPOINT = "endpoint"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val spinner: Spinner = findViewById(R.id.spinnerProvider)
        val etApiKey: EditText = findViewById(R.id.etApiKey)
        val etEndpoint: EditText = findViewById(R.id.etEndpoint)
        val btnSave: Button = findViewById(R.id.btnSave)

        val providers = listOf("OpenAI (preset)", "HuggingFace (preset)", "Cohere (preset)", "Perplexity", "Custom")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, providers)

        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val savedProvider = prefs.getString(KEY_PROVIDER, providers[0])
        val savedKey = prefs.getString(KEY_APIKEY, "")
        val savedEndpoint = prefs.getString(KEY_ENDPOINT, "")
        spinner.setSelection(providers.indexOf(savedProvider ?: providers[0]))
        etApiKey.setText(savedKey)
        etEndpoint.setText(savedEndpoint)

        btnSave.setOnClickListener {
            prefs.edit()
                .putString(KEY_PROVIDER, spinner.selectedItem as String)
                .putString(KEY_APIKEY, etApiKey.text.toString().trim())
                .putString(KEY_ENDPOINT, etEndpoint.text.toString().trim())
                .apply()
            finish()
        }
    }
}
