package com.asistenalya.manager

import com.asistenalya.domain.model.AssistantState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AssistantStateManager {

    private val _state = MutableStateFlow(AssistantState.READY)
    val state: StateFlow<AssistantState> = _state.asStateFlow()

    private val _lastCommand = MutableStateFlow("")
    val lastCommand: StateFlow<String> = _lastCommand.asStateFlow()

    private val _continuousListening = MutableStateFlow(false)
    val continuousListening: StateFlow<Boolean> = _continuousListening.asStateFlow()

    fun updateState(newState: AssistantState) {
        _state.value = newState
    }

    fun updateLastCommand(command: String) {
        _lastCommand.value = command
    }

    fun toggleContinuousListening() {
        _continuousListening.value = !_continuousListening.value
    }

    fun setContinuousListening(enabled: Boolean) {
        _continuousListening.value = enabled
    }
}
