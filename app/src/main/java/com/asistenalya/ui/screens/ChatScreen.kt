package com.asistenalya.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.asistenalya.data.repository.AIRepository
import com.asistenalya.domain.model.AssistantState
import com.asistenalya.domain.model.ChatMessage
import com.asistenalya.manager.AssistantStateManager
import com.asistenalya.manager.SpeechManager
import com.asistenalya.ui.components.TypingIndicator
import com.asistenalya.ui.theme.BlueGlow
import com.asistenalya.ui.theme.LightWhite
import com.asistenalya.ui.theme.NavyCard
import com.asistenalya.ui.theme.NavyDark
import com.asistenalya.ui.theme.NavyMedium
import com.asistenalya.ui.theme.PurplePastel
import com.asistenalya.ui.theme.SakuraPink
import com.asistenalya.ui.theme.SubtleGray
import com.asistenalya.utils.SecurePrefs
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val aiRepository = remember { AIRepository() }
    val speechManager = remember { SpeechManager(context) }
    val listState = rememberLazyListState()

    val messages = remember { mutableStateListOf<ChatMessage>() }
    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    val assistantState by AssistantStateManager.state.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            speechManager.startListening()
        } else {
            Toast.makeText(context, "Izin mikrofon diperlukan", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        speechManager.onResult = { text ->
            inputText = text
        }
        onDispose {
            speechManager.destroy()
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val userMessage = ChatMessage(content = text, isUser = true)
        messages.add(userMessage)
        inputText = ""
        isTyping = true
        AssistantStateManager.updateState(AssistantState.THINKING)

        scope.launch {
            listState.animateScrollToItem(messages.size - 1)
            val apiKey = SecurePrefs.getApiKey(context)
            if (apiKey.isEmpty()) {
                messages.add(ChatMessage(content = "API key belum diatur.", isUser = false))
                isTyping = false
                AssistantStateManager.updateState(AssistantState.READY)
                return@launch
            }
            when (val result = aiRepository.sendMessage(apiKey, text)) {
                is AIRepository.AIResult.Success -> {
                    messages.add(ChatMessage(content = result.text, isUser = false))
                }
                is AIRepository.AIResult.Error -> {
                    messages.add(ChatMessage(content = "Error: ${result.message}", isUser = false))
                }
            }
            isTyping = false
            AssistantStateManager.updateState(AssistantState.READY)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyDark, NavyMedium, NavyDark)
                )
            )
            .imePadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LightWhite)
            }
            Text(
                "Chat dengan Alya",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = SakuraPink
            )
        }

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message)
            }
            if (isTyping) {
                item {
                    TypingIndicator(modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        // Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ketik pesan...", color = SubtleGray) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LightWhite,
                    unfocusedTextColor = LightWhite,
                    focusedBorderColor = SakuraPink,
                    unfocusedBorderColor = SubtleGray.copy(alpha = 0.3f),
                    cursorColor = SakuraPink
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Mic button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PurplePastel)
                    .clickable {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                            == PackageManager.PERMISSION_GRANTED
                        ) {
                            speechManager.startListening()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Mic", tint = LightWhite, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Send button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(SakuraPink, PurplePastel)
                        )
                    )
                    .clickable { sendMessage(inputText) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = LightWhite, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(
                    if (isUser) {
                        Brush.linearGradient(
                            colors = listOf(SakuraPink.copy(alpha = 0.8f), PurplePastel.copy(alpha = 0.6f))
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(NavyCard, BlueGlow.copy(alpha = 0.15f))
                        )
                    },
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                color = LightWhite,
                fontSize = 14.sp
            )
        }
    }
}
