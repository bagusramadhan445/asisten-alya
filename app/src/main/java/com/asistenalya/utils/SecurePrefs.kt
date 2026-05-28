package com.asistenalya.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurePrefs {

    private const val FILE_NAME = "alya_secure_prefs"
    private const val KEY_API_KEY = "gemini_api_key"
    private const val KEY_ONBOARDED = "onboarded"
    private const val KEY_TTS_ENABLED = "tts_enabled"
    private const val KEY_VOICE_ENABLED = "voice_enabled"
    private const val KEY_OVERLAY_ENABLED = "overlay_enabled"
    private const val KEY_CONTINUOUS_LISTENING = "continuous_listening"

    private fun getPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveApiKey(context: Context, apiKey: String) {
        getPrefs(context).edit().putString(KEY_API_KEY, apiKey).apply()
    }

    fun getApiKey(context: Context): String {
        return getPrefs(context).getString(KEY_API_KEY, "") ?: ""
    }

    fun setOnboarded(context: Context, value: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ONBOARDED, value).apply()
    }

    fun isOnboarded(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ONBOARDED, false)
    }

    fun setTtsEnabled(context: Context, value: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_TTS_ENABLED, value).apply()
    }

    fun isTtsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_TTS_ENABLED, true)
    }

    fun setVoiceEnabled(context: Context, value: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_VOICE_ENABLED, value).apply()
    }

    fun isVoiceEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_VOICE_ENABLED, true)
    }

    fun setOverlayEnabled(context: Context, value: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_OVERLAY_ENABLED, value).apply()
    }

    fun isOverlayEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_OVERLAY_ENABLED, false)
    }

    fun setContinuousListening(context: Context, value: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_CONTINUOUS_LISTENING, value).apply()
    }

    fun isContinuousListening(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_CONTINUOUS_LISTENING, false)
    }

    fun clearAll(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
