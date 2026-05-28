package com.asistenalya.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.asistenalya.data.local.AlyaDatabase
import com.asistenalya.data.local.ChatEntity
import com.asistenalya.data.repository.AIRepository
import com.asistenalya.domain.model.AssistantState
import com.asistenalya.domain.model.ChatMessage
import com.asistenalya.manager.AssistantStateManager
import com.asistenalya.utils.SecurePrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AlyaDatabase.getInstance(application)
    private val chatDao = db.chatDao()
    private val aiRepository = AIRepository()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    init {
        viewModelScope.launch {
            chatDao.getAllMessages().collect { entities ->
                _messages.value = entities.map { entity ->
                    ChatMessage(
                        id = entity.id,
                        content = entity.content,
                        isUser = entity.isUser,
                        timestamp = entity.timestamp
                    )
                }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            chatDao.insertMessage(
                ChatEntity(content = text, isUser = true)
            )

            _isTyping.value = true
            AssistantStateManager.updateState(AssistantState.THINKING)

            val apiKey = SecurePrefs.getApiKey(getApplication())
            if (apiKey.isEmpty()) {
                chatDao.insertMessage(
                    ChatEntity(content = "API key belum diatur. Silakan atur di pengaturan.", isUser = false)
                )
                _isTyping.value = false
                AssistantStateManager.updateState(AssistantState.READY)
                return@launch
            }

            when (val result = aiRepository.sendMessage(apiKey, text)) {
                is AIRepository.AIResult.Success -> {
                    chatDao.insertMessage(
                        ChatEntity(content = result.text, isUser = false)
                    )
                }
                is AIRepository.AIResult.Error -> {
                    chatDao.insertMessage(
                        ChatEntity(content = "Error: ${result.message}", isUser = false)
                    )
                }
            }
            _isTyping.value = false
            AssistantStateManager.updateState(AssistantState.READY)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatDao.deleteAllMessages()
        }
    }
}
