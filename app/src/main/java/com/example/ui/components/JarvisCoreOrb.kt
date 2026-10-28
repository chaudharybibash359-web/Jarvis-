package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.AssistantState
import com.example.ui.theme.JarvisAccentGreen
import com.example.ui.theme.JarvisAccentRed
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisNeonBlue
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisCoreOrb(
    state: AssistantState,
    audioAmplitude: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_orb_transition")

    // Continuous smooth rotations for concentric rings
    val rotationAngleOuter by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rotation"
    )

    val rotationAngleMid by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mid_rotation"
    )

    val rotationAngleInner by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rotation"
    )

    // Base breathe pulse
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    // Active amplitude smoothing
    val smoothedAmplitude = remember { Animatable(0f) }
    LaunchedEffect(audioAmplitude) {
        smoothedAmplitude.animateTo(
            targetValue = audioAmplitude.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 80, easing = LinearEasing)
        )
    }

    val activeColor = when (state) {
        is AssistantState.Listening -> JarvisCyan
        is AssistantState.Thinking -> JarvisNeonBlue
        is AssistantState.Speaking -> JarvisAccentGreen
        is AssistantState.Error -> JarvisAccentRed
        AssistantState.Idle -> JarvisCyan
    }

    val primaryGlow = activeColor.copy(alpha = 0.45f)

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2.2f
            val dynamicScale = (breatheScale + smoothedAmplitude.value * 0.28f).coerceIn(0.85f, 1.4f)
            val activeRadius = baseRadius * dynamicScale

            // 1. Outermost subtle ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryGlow.copy(alpha = 0.35f + smoothedAmplitude.value * 0.3f),
                        activeColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = activeRadius * 1.35f
                ),
                radius = activeRadius * 1.35f,
                center = centerOffset
            )

            // 2. Outermost rotating dashed telemetry ring
            rotate(rotationAngleOuter, pivot = centerOffset) {
                drawCircle(
                    color = activeColor.copy(alpha = 0.35f),
                    radius = activeRadius * 0.96f,
                    center = centerOffset,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f, 6f, 14f), 0f)
                    )
                )

                // 4 outer orbital telemetry markers
                for (i in 0 until 4) {
                    val angle = (i * 90.0) * (Math.PI / 180.0)
                    val markerX = centerOffset.x + (activeRadius * 0.96f * cos(angle)).toFloat()
                    val markerY = centerOffset.y + (activeRadius * 0.96f * sin(angle)).toFloat()
                    drawCircle(
                        color = JarvisCyanLight,
                        radius = 3.5.dp.toPx(),
                        center = Offset(markerX, markerY)
                    )
                }
            }

            // 3. Middle ring with counter-rotation and segment ticks
            rotate(rotationAngleMid, pivot = centerOffset) {
                drawCircle(
                    color = activeColor.copy(alpha = 0.65f),
                    radius = activeRadius * 0.78f,
                    center = centerOffset,
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(35f, 12f), 0f)
                    )
                )
            }

            // 4. Inner fast rotating ring
            rotate(rotationAngleInner, pivot = centerOffset) {
                drawCircle(
                    color = activeColor.copy(alpha = 0.85f),
                    radius = activeRadius * 0.58f,
                    center = centerOffset,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 8f, 12f, 8f), 0f)
                    )
                )
            }

            // 5. Central Reactor Glowing Core
            val coreRadius = activeRadius * 0.42f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        JarvisCyanLight,
                        activeColor,
                        activeColor.copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = coreRadius * 1.25f
                ),
                radius = coreRadius,
                center = centerOffset
            )

            // High-contrast sharp center core border
            drawCircle(
                color = JarvisCyanLight,
                radius = coreRadius * 0.85f,
                center = centerOffset,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Center Icon based on state
        val iconSize = (size * 0.22f).coerceAtLeast(36.dp)
        when (state) {
            is AssistantState.Listening -> {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Listening",
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
                )
            }
            is AssistantState.Speaking -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Speaking",
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
                )
            }
            is AssistantState.Thinking -> {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Processing",
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Tap to speak",
                    tint = JarvisCyanLight,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}
