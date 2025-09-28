# PromptGen — Android (Kotlin) Prompt-Generation App

This project is a minimal, complete Android Studio project that implements a native Android app to:
- Take a user's rough idea
- Detect a theme (on-device heuristic)
- Call a configurable LLM endpoint (OpenAI/HuggingFace/Cohere or custom)
- Return a *refined prompt* (the app enforces the output to be a prompt)

## What's included
- Full Android Studio project under `/app`
- Simple UI: input, generate button, output, copy, settings
- Network code using OkHttp (no external LLM SDKs included)
- Settings to store API key and custom endpoint (SharedPreferences)

## How to open in Android Studio
1. Download the ZIP: `prompt-gen-app.zip`
2. Open Android Studio -> File -> Open... and select the extracted folder `PromptGenApp` (the folder that contains `settings.gradle`).
3. Let Gradle sync/download dependencies.
4. Connect an Android device or run an emulator and press Run.

## Setting up an LLM provider
- Open `Settings` in the app.
- Choose a preset (OpenAI / HuggingFace / Cohere) or Custom.
- Paste your API key (Bearer token) and optional custom endpoint.
- For OpenAI, use endpoint: `https://api.openai.com/v1/chat/completions`
- For Cohere: `https://api.cohere.ai/generate`
- For HuggingFace inference, set endpoint to a model-specific inference URL, e.g. `https://api-inference.huggingface.co/models/<model>`

## Notes & next steps
- This is intentionally simple to be a clear starting point.
- You can replace the lightweight theme classifier with an on-device TFLite model.
- Consider switching to DataStore for robust settings and encrypting stored API keys for production (e.g., EncryptedSharedPreferences).
- Add proper error handling, retries, and UI polish.