package com.asistenalya.manager

import android.content.Context
import android.widget.Toast
import com.asistenalya.data.repository.AIRepository
import com.asistenalya.domain.model.AssistantState
import com.asistenalya.utils.SecurePrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IntentRouter(
    private val context: Context,
    private val appLauncher: AppLauncher,
    private val ttsManager: TTSManager,
    private val powerManager: AlyaPowerManager,
    private val aiRepository: AIRepository
) {

    fun route(intent: ParsedIntent) {
        when (intent) {
            is ParsedIntent.OpenApp -> {
                val success = appLauncher.launch(intent.packageName)
                if (success) {
                    ttsManager.speak("Oke, aku bukain ${intent.appName} ya.")
                } else {
                    ttsManager.speak("Maaf, aplikasi ${intent.appName} tidak ditemukan.")
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "Aplikasi tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            is ParsedIntent.PowerAction -> {
                when (intent.action) {
                    PowerActionType.LOCK_SCREEN -> {
                        val success = powerManager.lockScreen()
                        if (success) {
                            ttsManager.speak("Oke, layar dikunci.")
                        } else {
                            ttsManager.speak("Maaf, aku butuh izin Device Admin untuk mengunci layar.")
                        }
                    }

                    PowerActionType.POWER_DIALOG -> {
                        val success = powerManager.openPowerDialog()
                        if (success) {
                            ttsManager.speak("Membuka menu daya.")
                        } else {
                            ttsManager.speak("Maaf, aku butuh izin Accessibility untuk membuka menu daya.")
                        }
                    }
                }
            }

            is ParsedIntent.AskAI -> {
                AssistantStateManager.updateState(AssistantState.THINKING)
                val apiKey = SecurePrefs.getApiKey(context)
                if (apiKey.isEmpty()) {
                    ttsManager.speak("API key belum diatur. Silakan atur di pengaturan.")
                    AssistantStateManager.updateState(AssistantState.READY)
                    return
                }

                CoroutineScope(Dispatchers.Main).launch {
                    when (val result = aiRepository.sendMessage(apiKey, intent.query)) {
                        is AIRepository.AIResult.Success -> {
                            ttsManager.speak(result.text)
                        }

                        is AIRepository.AIResult.Error -> {
                            ttsManager.speak("Maaf, terjadi kesalahan: ${result.message}")
                            AssistantStateManager.updateState(AssistantState.ERROR)
                        }
                    }
                }
            }
        }
    }
}
