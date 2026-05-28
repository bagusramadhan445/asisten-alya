package com.asistenalya.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.asistenalya.manager.AlyaPowerManager
import com.asistenalya.manager.AppLauncher
import com.asistenalya.manager.AssistantStateManager
import com.asistenalya.manager.IntentParser
import com.asistenalya.manager.IntentRouter
import com.asistenalya.manager.SpeechManager
import com.asistenalya.manager.TTSManager
import com.asistenalya.ui.components.GlowCard
import com.asistenalya.ui.components.GlowingOrb
import com.asistenalya.ui.theme.BlueGlow
import com.asistenalya.ui.theme.LightWhite
import com.asistenalya.ui.theme.NavyDark
import com.asistenalya.ui.theme.NavyMedium
import com.asistenalya.ui.theme.PurplePastel
import com.asistenalya.ui.theme.SakuraPink
import com.asistenalya.ui.theme.SubtleGray

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToApps: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val assistantState by AssistantStateManager.state.collectAsState()
    val lastCommand by AssistantStateManager.lastCommand.collectAsState()
    val continuousListening by AssistantStateManager.continuousListening.collectAsState()

    val ttsManager = remember { TTSManager(context) }
    val speechManager = remember { SpeechManager(context) }
    val appLauncher = remember { AppLauncher(context) }
    val powerManager = remember { AlyaPowerManager(context) }
    val aiRepository = remember { AIRepository() }
    val intentRouter = remember {
        IntentRouter(context, appLauncher, ttsManager, powerManager, aiRepository)
    }

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
            val intent = IntentParser.parse(text)
            intentRouter.route(intent)
        }
        onDispose {
            speechManager.destroy()
            ttsManager.shutdown()
        }
    }

    fun startVoice() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            speechManager.startListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyDark, NavyMedium, NavyDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Asisten Alya",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = SakuraPink
                )
                Row {
                    IconButton(onClick = onNavigateToChat) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat", tint = BlueGlow)
                    }
                    IconButton(onClick = onNavigateToApps) {
                        Icon(Icons.Default.Apps, contentDescription = "Apps", tint = PurplePastel)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = SubtleGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Orb
            GlowingOrb(
                state = assistantState,
                size = 200.dp,
                onClick = { startVoice() },
                onLongPress = {
                    AssistantStateManager.toggleContinuousListening()
                    val status = if (AssistantStateManager.continuousListening.value)
                        "Continuous listening aktif" else "Continuous listening nonaktif"
                    Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
                    if (AssistantStateManager.continuousListening.value) {
                        startVoice()
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (assistantState) {
                    AssistantState.READY -> "Tap orb untuk mulai"
                    AssistantState.LISTENING -> "Mendengarkan..."
                    AssistantState.THINKING -> "Berpikir..."
                    AssistantState.SPEAKING -> "Berbicara..."
                    AssistantState.ERROR -> "Terjadi kesalahan"
                },
                color = when (assistantState) {
                    AssistantState.READY -> SubtleGray
                    AssistantState.LISTENING -> BlueGlow
                    AssistantState.THINKING -> PurplePastel
                    AssistantState.SPEAKING -> SakuraPink
                    AssistantState.ERROR -> com.asistenalya.ui.theme.ErrorRed
                },
                fontSize = 14.sp
            )

            if (continuousListening) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(BlueGlow)
                    )
                    Text("Continuous Listening", color = BlueGlow, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI Status Card
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when (assistantState) {
                                        AssistantState.READY -> com.asistenalya.ui.theme.SuccessGreen
                                        AssistantState.ERROR -> com.asistenalya.ui.theme.ErrorRed
                                        else -> BlueGlow
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "AI Status",
                            fontWeight = FontWeight.SemiBold,
                            color = LightWhite,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Status: ${assistantState.name}",
                        color = SubtleGray,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Model: gemini-2.0-flash",
                        color = SubtleGray,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Command Card
            GlowCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Perintah Terakhir",
                        fontWeight = FontWeight.SemiBold,
                        color = PurplePastel,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (lastCommand.isEmpty()) "Belum ada perintah" else lastCommand,
                        color = if (lastCommand.isEmpty()) SubtleGray else LightWhite,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Quick Actions",
                        fontWeight = FontWeight.SemiBold,
                        color = BlueGlow,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val quickApps = listOf(
                            "WhatsApp" to "com.whatsapp",
                            "TikTok" to "com.zhiliaoapp.musically",
                            "Instagram" to "com.instagram.android",
                            "YouTube" to "com.google.android.youtube",
                            "Chrome" to "com.android.chrome"
                        )
                        quickApps.forEach { (name, pkg) ->
                            AssistChip(
                                onClick = {
                                    val success = appLauncher.launch(pkg)
                                    if (!success) {
                                        Toast.makeText(context, "Aplikasi tidak ditemukan", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                label = { Text(name, fontSize = 12.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = SakuraPink.copy(alpha = 0.15f),
                                    labelColor = SakuraPink
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mic Button
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(SakuraPink, PurplePastel)
                        )
                    )
                    .clickable { startVoice() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (assistantState == AssistantState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice",
                    tint = LightWhite,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
