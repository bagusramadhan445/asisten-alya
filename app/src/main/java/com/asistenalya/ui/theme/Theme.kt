package com.asistenalya.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AlyaDarkColorScheme = darkColorScheme(
    primary = SakuraPink,
    onPrimary = LightWhite,
    primaryContainer = SakuraPinkDark,
    onPrimaryContainer = LightWhite,
    secondary = PurplePastel,
    onSecondary = LightWhite,
    secondaryContainer = PurpleDeep,
    onSecondaryContainer = LightWhite,
    tertiary = BlueGlow,
    onTertiary = LightWhite,
    background = NavyDark,
    onBackground = LightWhite,
    surface = NavySurface,
    onSurface = LightWhite,
    surfaceVariant = NavyCard,
    onSurfaceVariant = SubtleGray,
    error = ErrorRed,
    onError = LightWhite
)

@Composable
fun AsistenAlyaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AlyaDarkColorScheme,
        typography = AlyaTypography,
        content = content
    )
}
