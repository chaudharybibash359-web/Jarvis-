package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AssistantState
import com.example.ui.theme.JarvisAccentGreen
import com.example.ui.theme.JarvisAccentRed
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisNeonBlue
import com.example.ui.theme.JarvisSurfaceVariant

@Composable
fun StatusIndicator(
    state: AssistantState,
    assistantName: String = "JARVIS",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_dot")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    val (statusLabel, statusColor) = when (state) {
        is AssistantState.Listening -> "LISTENING // CAPTURING VOICE" to JarvisCyan
        is AssistantState.Thinking -> "PROCESSING // NEURAL SYNTHESIS" to JarvisNeonBlue
        is AssistantState.Speaking -> "VOCALIZING // RESPONSE ACTIVE" to JarvisAccentGreen
        is AssistantState.Error -> "ALERT // PROTOCOL FAULT" to JarvisAccentRed
        AssistantState.Idle -> "$assistantName // READY FOR INPUT" to JarvisCyan.copy(alpha = 0.8f)
    }

    Row(
        modifier = modifier
            .background(JarvisSurfaceVariant.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(dotAlpha)
                .background(statusColor, CircleShape)
        )
        Text(
            text = statusLabel,
            color = statusColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}
