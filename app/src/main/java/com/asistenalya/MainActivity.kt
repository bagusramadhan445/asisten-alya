package com.asistenalya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
