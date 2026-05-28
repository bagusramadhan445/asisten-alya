package com.asistenalya.manager

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.asistenalya.domain.model.AssistantState
import java.util.Locale
import java.util.UUID

class TTSManager(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.language = Locale("id", "ID")
                tts?.setSpeechRate(1.0f)
                tts?.setPitch(1.1f)

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        AssistantStateManager.updateState(AssistantState.SPEAKING)
                    }

                    override fun onDone(utteranceId: String?) {
                        AssistantStateManager.updateState(AssistantState.READY)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        AssistantStateManager.updateState(AssistantState.ERROR)
                    }
                })
            }
        }
    }

    fun speak(text: String) {
        if (!isInitialized) return
        val utteranceId = UUID.randomUUID().toString()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
