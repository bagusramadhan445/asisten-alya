package com.asistenalya

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.asistenalya.ui.screens.AppsScreen
import com.asistenalya.ui.screens.ChatScreen
import com.asistenalya.ui.screens.HomeScreen
import com.asistenalya.ui.screens.OnboardingScreen
import com.asistenalya.ui.screens.SettingsScreen
import com.asistenalya.ui.theme.AsistenAlyaTheme
import com.asistenalya.ui.theme.NavyDark
import com.asistenalya.utils.SecurePrefs

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()

        setContent {
            AsistenAlyaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NavyDark
                ) {
                    val isOnboarded = SecurePrefs.isOnboarded(this)
                    AlyaNavigation(startOnboarding = !isOnboarded)
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun AlyaNavigation(startOnboarding: Boolean) {
    val navController = rememberNavController()
    val startDestination = if (startOnboarding) "onboarding" else "home"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                onNavigateToChat = { navController.navigate("chat") },
                onNavigateToApps = { navController.navigate("apps") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("chat") {
            ChatScreen(onBack = { navController.popBackStack() })
        }

        composable("apps") {
            AppsScreen(onBack = { navController.popBackStack() })
        }

        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
