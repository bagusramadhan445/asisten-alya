package com.asistenalya.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asistenalya.data.repository.AIRepository
import com.asistenalya.domain.model.AssistantState
import com.asistenalya.ui.components.GlowingOrb
import com.asistenalya.ui.theme.BlueGlow
import com.asistenalya.ui.theme.LightWhite
import com.asistenalya.ui.theme.NavyDark
import com.asistenalya.ui.theme.NavyMedium
import com.asistenalya.ui.theme.PurplePastel
import com.asistenalya.ui.theme.SakuraPink
import com.asistenalya.ui.theme.SubtleGray
import com.asistenalya.ui.theme.SuccessGreen
import com.asistenalya.utils.SecurePrefs
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onNavigateToHome: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val aiRepository = remember { AIRepository() }

    var apiKey by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var connectionStatus by remember { mutableStateOf<Boolean?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        NavyDark,
                        NavyMedium,
                        NavyDark
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GlowingOrb(
                state = when {
                    isLoading -> AssistantState.THINKING
                    connectionStatus == true -> AssistantState.READY
                    connectionStatus == false -> AssistantState.ERROR
                    else -> AssistantState.READY
                },
                size = 140.dp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Asisten Alya",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = SakuraPink,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "AI Assistant Futuristik",
                fontSize = 16.sp,
                color = PurplePastel,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("Gemini API Key", color = SubtleGray) },
                leadingIcon = {
                    Icon(Icons.Default.Key, contentDescription = null, tint = SakuraPink)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LightWhite,
                    unfocusedTextColor = LightWhite,
                    focusedBorderColor = SakuraPink,
                    unfocusedBorderColor = SubtleGray.copy(alpha = 0.5f),
                    cursorColor = SakuraPink
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = clipboard.primaryClip
                        if (clip != null && clip.itemCount > 0) {
                            apiKey = clip.getItemAt(0).text.toString()
                            Toast.makeText(context, "API key berhasil dipaste", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Clipboard kosong", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BlueGlow)
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Paste Key", fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        if (apiKey.isBlank()) {
                            Toast.makeText(context, "Masukkan API key terlebih dahulu", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true
                        connectionStatus = null
                        scope.launch {
                            val success = aiRepository.testConnection(apiKey.trim())
                            isLoading = false
                            connectionStatus = success
                            if (success) {
                                SecurePrefs.saveApiKey(context, apiKey.trim())
                                SecurePrefs.setOnboarded(context, true)
                                Toast.makeText(context, "Gemini AI connected", Toast.LENGTH_SHORT).show()
                                onNavigateToHome()
                            } else {
                                Toast.makeText(context, "Koneksi gagal. Periksa API key.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SakuraPink),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = LightWhite,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = connectionStatus != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = if (connectionStatus == true) "Gemini AI Connected" else "Koneksi Gagal",
                    color = if (connectionStatus == true) SuccessGreen else com.asistenalya.ui.theme.ErrorRed,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
