package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.domain.model.AssistantState
import com.example.ui.theme.JarvisAccentGreen
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisNeonBlue
import kotlin.math.sin

@Composable
fun VoiceWaveVisualizer(
    state: AssistantState,
    amplitude: Float,
    modifier: Modifier = Modifier,
    barCount: Int = 28
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val smoothedAmp = remember { Animatable(0.05f) }
    LaunchedEffect(amplitude) {
        smoothedAmp.animateTo(
            targetValue = amplitude.coerceIn(0.05f, 1f),
            animationSpec = tween(durationMillis = 60, easing = LinearEasing)
        )
    }

    val waveColor = when (state) {
        is AssistantState.Listening -> JarvisCyan
        is AssistantState.Speaking -> JarvisAccentGreen
        is AssistantState.Thinking -> JarvisNeonBlue
        else -> JarvisCyan.copy(alpha = 0.4f)
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val barWidth = (width / (barCount * 1.6f)).coerceIn(4f, 14f)
        val spacing = (width - (barCount * barWidth)) / (barCount + 1)

        val isActive = state is AssistantState.Listening || state is AssistantState.Speaking || state is AssistantState.Thinking
        val currentAmp = if (isActive) smoothedAmp.value else 0.08f

        for (i in 0 until barCount) {
            val normalizedX = i.toFloat() / barCount.toFloat()
            // Sine wave envelope combined with phase
            val sineVal = sin((normalizedX * 4 * Math.PI + phase).toDouble()).toFloat()
            val bellCurve = (1f - (2f * (normalizedX - 0.5f)).let { it * it }).coerceIn(0.15f, 1f)
            val barAmp = ((sineVal * 0.4f + 0.6f) * bellCurve * currentAmp).coerceIn(0.06f, 1f)
            val barHeight = (height * 0.85f * barAmp).coerceAtLeast(6f)

            val x = spacing + i * (barWidth + spacing)
            val top = centerY - (barHeight / 2f)

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        JarvisCyanLight,
                        waveColor,
                        waveColor.copy(alpha = 0.5f)
                    ),
                    startY = top,
                    endY = top + barHeight
                ),
                topLeft = Offset(x, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}
