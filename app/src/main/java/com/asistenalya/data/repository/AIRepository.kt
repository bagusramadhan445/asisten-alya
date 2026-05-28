package com.asistenalya.data.repository

import com.asistenalya.ai.GeminiProvider
import com.asistenalya.domain.model.Content
import com.asistenalya.domain.model.GeminiRequest
import com.asistenalya.domain.model.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIRepository {

    private val apiService = GeminiProvider.apiService

    sealed class AIResult {
        data class Success(val text: String) : AIResult()
        data class Error(val message: String) : AIResult()
    }

    suspend fun sendMessage(apiKey: String, message: String): AIResult {
        return withContext(Dispatchers.IO) {
            try {
                val request = GeminiRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(Part(text = message))
                        )
                    )
                )
                val response = apiService.generateContent(apiKey, request)
                val responseText = response.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text

                if (responseText != null) {
                    AIResult.Success(responseText)
                } else {
                    AIResult.Error("Tidak ada respons dari Gemini AI")
                }
            } catch (e: Exception) {
                AIResult.Error(e.message ?: "Terjadi kesalahan koneksi")
            }
        }
    }

    suspend fun testConnection(apiKey: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = GeminiRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(Part(text = "Hello, respond with OK"))
                        )
                    )
                )
                val response = apiService.generateContent(apiKey, request)
                response.candidates?.isNotEmpty() == true
            } catch (e: Exception) {
                false
            }
        }
    }
}
