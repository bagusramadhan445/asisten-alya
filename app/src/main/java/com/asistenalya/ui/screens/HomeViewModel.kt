package com.asistenalya.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.asistenalya.data.local.AlyaDatabase
import com.asistenalya.domain.model.AssistantState
import com.asistenalya.manager.AssistantStateManager
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AlyaDatabase.getInstance(application)

    val assistantState: StateFlow<AssistantState> = AssistantStateManager.state
    val lastCommand: StateFlow<String> = AssistantStateManager.lastCommand
    val continuousListening: StateFlow<Boolean> = AssistantStateManager.continuousListening
}
