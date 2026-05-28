package com.asistenalya.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.asistenalya.domain.model.AssistantState
import com.asistenalya.ui.theme.BlueGlow
import com.asistenalya.ui.theme.ErrorRed
import com.asistenalya.ui.theme.PurplePastel
import com.asistenalya.ui.theme.SakuraPink

@Composable
fun GlowingOrb(
    state: AssistantState,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    onClick: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")

    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantState.LISTENING -> 400
                    AssistantState.THINKING -> 800
                    AssistantState.SPEAKING -> 600
                    else -> 2000
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotationAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val (primaryColor, secondaryColor, glowAlpha) = when (state) {
        AssistantState.READY -> Triple(SakuraPink, SakuraPink.copy(alpha = 0.3f), 0.2f)
        AssistantState.LISTENING -> Triple(BlueGlow, BlueGlow.copy(alpha = 0.5f), 0.4f)
        AssistantState.THINKING -> Triple(PurplePastel, PurplePastel.copy(alpha = 0.5f), 0.35f)
        AssistantState.SPEAKING -> Triple(SakuraPink, SakuraPink.copy(alpha = 0.5f), 0.4f)
        AssistantState.ERROR -> Triple(ErrorRed, ErrorRed.copy(alpha = 0.4f), 0.3f)
    }

    Canvas(
        modifier = modifier
            .size(size)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = this.size.minDimension / 2f

        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryColor.copy(alpha = glowAlpha * pulseAnim),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 1.5f * pulseAnim
            ),
            radius = radius * 1.5f * pulseAnim,
            center = center
        )

        // Middle glow ring
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.3f * pulseAnim),
                    primaryColor.copy(alpha = 0.1f),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 1.2f
            ),
            radius = radius * 1.2f,
            center = center
        )

        // Main orb
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor,
                    primaryColor.copy(alpha = 0.7f),
                    primaryColor.copy(alpha = 0.3f),
                    Color.Transparent
                ),
                center = center,
                radius = radius * pulseAnim
            ),
            radius = radius * 0.7f * pulseAnim,
            center = center
        )

        // Inner bright core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.8f),
                    primaryColor.copy(alpha = 0.6f),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 0.3f
            ),
            radius = radius * 0.3f * pulseAnim,
            center = center
        )

        // Rotating highlight for THINKING state
        if (state == AssistantState.THINKING) {
            val offsetX = kotlin.math.cos(Math.toRadians(rotationAnim.toDouble())).toFloat() * radius * 0.3f
            val offsetY = kotlin.math.sin(Math.toRadians(rotationAnim.toDouble())).toFloat() * radius * 0.3f
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = radius * 0.15f,
                center = Offset(center.x + offsetX, center.y + offsetY)
            )
        }
    }
}
