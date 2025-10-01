package com.buddingintents.promptgen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.ProgressBar
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError

class MainActivity : AppCompatActivity() {

    private lateinit var etInput: EditText
    private lateinit var btnGenerate: Button
    private lateinit var tvOutput: TextView
    private lateinit var btnCopy: Button
    private lateinit var btnSettings: Button
    private lateinit var tvTheme: TextView
    private lateinit var progress: ProgressBar
    private lateinit var repo: LLMRepository
    private lateinit var btnShare: Button
    private lateinit var btnHistory: Button
    private lateinit var tvProviderStatus: TextView

    val analytics = Firebase.analytics
    var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        MobileAds.initialize(this) { initializationStatus ->
            // Optional: handle status if needed
            println("AdMob initialized: $initializationStatus")
        }

        val adView: AdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        loadInterstitialAd()

        initializeViews()
        setupClickListeners()
        updateProviderStatus()
    }

    override fun onResume() {
        super.onResume()
        updateProviderStatus()
    }

    private fun initializeViews() {
        etInput = findViewById(R.id.etInput)
        btnGenerate = findViewById(R.id.btnGenerate)
        tvOutput = findViewById(R.id.tvOutput)
        btnCopy = findViewById(R.id.btnCopy)
        btnSettings = findViewById(R.id.btnSettings)
        tvTheme = findViewById(R.id.tvTheme)
        progress = findViewById(R.id.progressBar)
        repo = LLMRepository(this)
        btnShare = findViewById(R.id.btnShare)
        btnHistory = findViewById(R.id.btnHistory)
        tvProviderStatus = findViewById(R.id.tvProviderStatus)

        tvOutput.movementMethod = ScrollingMovementMethod()
    }

    private fun setupClickListeners() {
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnGenerate.setOnClickListener {
            val text = etInput.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Please enter some text", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            generatePrompt(text)
        }

        btnCopy.setOnClickListener {
            // Log button click
            analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
                param(FirebaseAnalytics.Param.ITEM_ID, "btnCopy")
                param(FirebaseAnalytics.Param.ITEM_NAME, "copy")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "button")
            }

            val out = tvOutput.text.toString()
            if (out.isNotBlank()) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Prompt", out))
                Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }

        etInput.setOnEditorActionListener { _, actionId, _ ->
            analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
                this.param(FirebaseAnalytics.Param.ITEM_ID, "etInput")
                this.param(FirebaseAnalytics.Param.ITEM_NAME, "etInput")
                this.param(FirebaseAnalytics.Param.CONTENT_TYPE, "button")
            }

            if (actionId == EditorInfo.IME_ACTION_GO) {
                btnGenerate.performClick()
                true
            } else false
        }

        btnShare.setOnClickListener {
            analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
                this.param(FirebaseAnalytics.Param.ITEM_ID, "btnShare")
                this.param(FirebaseAnalytics.Param.ITEM_NAME, "btnShare")
                this.param(FirebaseAnalytics.Param.CONTENT_TYPE, "button")
            }

            val promptText = tvOutput.text.toString()
            if (promptText.isNotBlank()) {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, promptText)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(sendIntent, "Share Prompt"))
            } else {
                Toast.makeText(this, "Nothing to share yet", Toast.LENGTH_SHORT).show()
            }
        }

        btnHistory.setOnClickListener {
            analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
                this.param(FirebaseAnalytics.Param.ITEM_ID, "btnHistory")
                this.param(FirebaseAnalytics.Param.ITEM_NAME, "btnHistory")
                this.param(FirebaseAnalytics.Param.CONTENT_TYPE, "button")
            }

            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun updateProviderStatus() {
        lifecycleScope.launch {
            val statusText = withContext(Dispatchers.Default) {
                try {
                    val provider = repo.getActiveProvider()
                    val isConfigured = repo.isProviderConfigured()

                    when {
                        isConfigured -> "Provider: ${provider.config.displayName} ✓"
                        else -> "Provider: ${provider.config.displayName} (not configured)"
                    }
                } catch (e: Exception) {
                    "Provider: Error loading"
                }
            }
            tvProviderStatus.text = statusText
        }
    }

    fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, "ca-app-pub-2020561089374332/7164290365", adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }
            })
    }

    fun showInterstitialAd() {
        mInterstitialAd?.show(this)
    }

    private fun generatePrompt(text: String) {
        tvOutput.text = ""
        tvTheme.text = "Theme: detecting..."
        progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val theme = classifyTheme(text)
                tvTheme.text = "Theme: $theme"

                val result = withContext(Dispatchers.IO) {
                    repo.generateRefinedPrompt(text, theme)
                }

                progress.visibility = View.GONE

                if (result.isSuccessful) {
                    val cleaned = Utils.ensurePromptOnly(result.text)
                    if (Utils.isValidPrompt(cleaned)) {
                        tvOutput.text = cleaned

                        // Save to history
                        HistoryStore.addRecord(
                            this@MainActivity,
                            PromptRecord(etInput.text.toString(), cleaned)
                        )

                        // Show token usage if available
                        result.usage?.let { usage ->
                            val usageText = "Tokens used: ${usage.totalTokens} (${usage.promptTokens}+${usage.completionTokens})"
                            Toast.makeText(this@MainActivity, usageText, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        tvOutput.text = "Generated content blocked (not a prompt). Try rephrasing or change provider in settings."
                    }
                } else {
                    val errorMessage = result.errorMessage ?: "Unknown error"
                    tvOutput.text = "Error: $errorMessage"

                    // Show helpful error message for common issues
                    when {
                        errorMessage.contains("API key") -> {
                            Toast.makeText(this@MainActivity, "Please configure API key in settings", Toast.LENGTH_LONG).show()
                        }
                        errorMessage.contains("401") -> {
                            Toast.makeText(this@MainActivity, "Invalid API key. Check settings.", Toast.LENGTH_LONG).show()
                        }
                        errorMessage.contains("429") -> {
                            Toast.makeText(this@MainActivity, "Rate limit reached. Try again later.", Toast.LENGTH_LONG).show()
                        }
                        errorMessage.contains("Network") -> {
                            Toast.makeText(this@MainActivity, "Network error. Check connection.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                progress.visibility = View.GONE
                tvOutput.text = "Error: ${e.message}"
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

    // Lightweight keyword-based theme classification (fast, on-device)
    private fun classifyTheme(text: String): String {
        val t = text.lowercase()
        val codeWords = listOf("code", "javascript", "python", "api", "sql", "android", "kotlin", "java")
        val storyWords = listOf("story", "novel", "character", "prose", "fiction", "plot", "scene")
        val marketing = listOf("ad", "advert", "marketing", "copy", "slogan", "headline", "seo", "blog")

        return when {
            codeWords.any { t.contains(it) } -> "code"
            storyWords.any { t.contains(it) } -> "story"
            marketing.any { t.contains(it) } -> "marketing"
            else -> "general"
        }
    }
}