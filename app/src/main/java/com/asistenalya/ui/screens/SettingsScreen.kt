package com.asistenalya.ui.screens

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asistenalya.service.AlyaDeviceAdminReceiver
import com.asistenalya.service.OverlayService
import com.asistenalya.ui.components.GlowCard
import com.asistenalya.ui.components.SectionTitle
import com.asistenalya.ui.theme.BlueGlow
import com.asistenalya.ui.theme.ErrorRed
import com.asistenalya.ui.theme.LightWhite
import com.asistenalya.ui.theme.NavyDark
import com.asistenalya.ui.theme.NavyMedium
import com.asistenalya.ui.theme.PurplePastel
import com.asistenalya.ui.theme.SakuraPink
import com.asistenalya.ui.theme.SubtleGray
import com.asistenalya.utils.SecurePrefs

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var ttsEnabled by remember { mutableStateOf(SecurePrefs.isTtsEnabled(context)) }
    var voiceEnabled by remember { mutableStateOf(SecurePrefs.isVoiceEnabled(context)) }
    var overlayEnabled by remember { mutableStateOf(SecurePrefs.isOverlayEnabled(context)) }
    var continuousListening by remember { mutableStateOf(SecurePrefs.isContinuousListening(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyDark, NavyMedium, NavyDark)
                )
            )
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
                "Pengaturan",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = SakuraPink
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // AI Setup
            SectionTitle("AI Setup")
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsItem(
                        icon = Icons.Default.Cloud,
                        title = "Model AI",
                        subtitle = "gemini-2.0-flash",
                        iconTint = BlueGlow
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsItem(
                        icon = Icons.Default.Security,
                        title = "API Key",
                        subtitle = if (SecurePrefs.getApiKey(context).isNotEmpty()) "Tersimpan" else "Belum diatur",
                        iconTint = PurplePastel
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Voice
            SectionTitle("Suara")
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsToggle(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = "Text to Speech",
                        subtitle = "Respons suara dari Alya",
                        checked = ttsEnabled,
                        onToggle = {
                            ttsEnabled = it
                            SecurePrefs.setTtsEnabled(context, it)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsToggle(
                        icon = Icons.Default.Mic,
                        title = "Voice Input",
                        subtitle = "Aktifkan pengenalan suara",
                        checked = voiceEnabled,
                        onToggle = {
                            voiceEnabled = it
                            SecurePrefs.setVoiceEnabled(context, it)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsToggle(
                        icon = Icons.Default.RecordVoiceOver,
                        title = "Continuous Listening",
                        subtitle = "Dengarkan terus menerus",
                        checked = continuousListening,
                        onToggle = {
                            continuousListening = it
                            SecurePrefs.setContinuousListening(context, it)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Permissions
            SectionTitle("Izin")
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsButton(
                        icon = Icons.Default.Layers,
                        title = "Overlay Permission",
                        subtitle = "Izin tampil di atas aplikasi",
                        buttonText = "Buka",
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsButton(
                        icon = Icons.Default.Accessibility,
                        title = "Accessibility Service",
                        subtitle = "Untuk kontrol daya",
                        buttonText = "Buka",
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsButton(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "Device Admin",
                        subtitle = "Untuk kunci layar",
                        buttonText = "Aktifkan",
                        onClick = {
                            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                putExtra(
                                    DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                                    ComponentName(context, AlyaDeviceAdminReceiver::class.java)
                                )
                                putExtra(
                                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                    "Asisten Alya membutuhkan izin ini untuk mengunci layar."
                                )
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingsButton(
                            icon = Icons.Default.Notifications,
                            title = "Notification Permission",
                            subtitle = "Izin notifikasi foreground",
                            buttonText = "Buka",
                            onClick = {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Appearance
            SectionTitle("Tampilan")
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsItem(
                        icon = Icons.Default.DarkMode,
                        title = "Tema",
                        subtitle = "Anime Futuristic Dark",
                        iconTint = PurplePastel
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "Animasi",
                        subtitle = "Glow & Pulse aktif",
                        iconTint = SakuraPink
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Memory
            SectionTitle("Memori")
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsItem(
                        icon = Icons.Default.Memory,
                        title = "Cache",
                        subtitle = "Data tersimpan lokal",
                        iconTint = BlueGlow
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsButton(
                        icon = Icons.Default.CleaningServices,
                        title = "Hapus Semua Data",
                        subtitle = "Reset API key & pengaturan",
                        buttonText = "Reset",
                        buttonColor = ErrorRed,
                        onClick = {
                            SecurePrefs.clearAll(context)
                            Toast.makeText(context, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Power Control
            SectionTitle("Power Control")
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsToggle(
                        icon = Icons.Default.PowerSettingsNew,
                        title = "Overlay Service",
                        subtitle = "Orb mengambang aktif",
                        checked = overlayEnabled,
                        onToggle = {
                            overlayEnabled = it
                            SecurePrefs.setOverlayEnabled(context, it)
                            if (it) {
                                if (Settings.canDrawOverlays(context)) {
                                    OverlayService.start(context)
                                } else {
                                    Toast.makeText(context, "Izin overlay belum diberikan", Toast.LENGTH_SHORT).show()
                                    overlayEnabled = false
                                    SecurePrefs.setOverlayEnabled(context, false)
                                }
                            } else {
                                OverlayService.stop(context)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Catatan: Android membatasi shutdown langsung dari aplikasi. " +
                                "Gunakan perintah 'menu daya' untuk membuka dialog power Android.",
                        color = SubtleGray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: androidx.compose.ui.graphics.Color = SakuraPink
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, color = LightWhite, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(subtitle, color = SubtleGray, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SettingsToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = null, tint = SakuraPink, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = LightWhite, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(subtitle, color = SubtleGray, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SakuraPink,
                checkedTrackColor = SakuraPink.copy(alpha = 0.3f),
                uncheckedThumbColor = SubtleGray,
                uncheckedTrackColor = SubtleGray.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun SettingsButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    buttonText: String,
    buttonColor: androidx.compose.ui.graphics.Color = BlueGlow,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = null, tint = SakuraPink, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = LightWhite, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(subtitle, color = SubtleGray, fontSize = 12.sp)
        }
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor.copy(alpha = 0.2f),
                contentColor = buttonColor
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(buttonText, fontSize = 12.sp)
        }
    }
}
