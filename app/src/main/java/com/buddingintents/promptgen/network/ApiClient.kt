package com.buddingintents.promptgen.network

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * HTTP response wrapper
 */
data class HttpResponse(
    val isSuccessful: Boolean,
    val code: Int,
    val body: String,
    val headers: Headers? = null
)

/**
 * Centralized HTTP client for API calls
 */
object ApiClient {
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .callTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    /**
     * Make a POST request
     */
    suspend fun post(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse {
        return executeRequest {
            Request.Builder()
                .url(url)
                .post(body.toRequestBody("application/json".toMediaType()))
                .apply {
                    headers.forEach { (key, value) ->
                        addHeader(key, value)
                    }
                }
                .build()
        }
    }

    /**
     * Make a GET request
     */
    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse {
        return executeRequest {
            Request.Builder()
                .url(url)
                .get()
                .apply {
                    headers.forEach { (key, value) ->
                        addHeader(key, value)
                    }
                }
                .build()
        }
    }

    private suspend fun executeRequest(requestBuilder: () -> Request): HttpResponse {
        return try {
            val request = requestBuilder()
            val response = client.newCall(request).execute()

            HttpResponse(
                isSuccessful = response.isSuccessful,
                code = response.code,
                body = response.body?.string() ?: "",
                headers = response.headers
            )
        } catch (e: IOException) {
            HttpResponse(
                isSuccessful = false,
                code = -1,
                body = "Network error: ${e.message}"
            )
        } catch (e: Exception) {
            HttpResponse(
                isSuccessful = false,
                code = -2,
                body = "Unknown error: ${e.message}"
            )
        }
    }
}