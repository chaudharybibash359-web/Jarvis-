package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MessageEntity
import com.example.domain.model.AssistantState
import com.example.ui.JarvisViewModel
import com.example.ui.components.JarvisCoreOrb
import com.example.ui.components.QuickActionChips
import com.example.ui.components.StatusIndicator
import com.example.ui.components.VoiceWaveVisualizer
import com.example.ui.theme.JarvisAccentGreen
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisNeonBlue
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun HomeScreen(
    state: AssistantState,
    amplitude: Float,
    partialText: String,
    recentMessages: List<MessageEntity>,
    settingsState: JarvisViewModel.SettingsState,
    onOrbClick: () -> Unit,
    onSendQuery: (String) -> Unit,
    onSpeakMessage: (String) -> Unit,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    val lastAssistantMessage = recentMessages.lastOrNull { !it.isUser }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        JarvisBackground,
                        Color(0xFF030914),
                        JarvisSurface
                    )
                )
            )
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top Telemetry Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SYSTEM IDENTIFIER",
                    color = JarvisCyan.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = settingsState.assistantName,
                    color = JarvisTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            Box(
                modifier = Modifier
                    .background(JarvisSurfaceVariant, RoundedCornerShape(12.dp))
                    .border(1.dp, JarvisCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(JarvisAccentGreen, CircleShape)
                    )
                    Text(
                        text = settingsState.selectedModel,
                        color = JarvisCyanLight,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Futuristic Status Indicator
        StatusIndicator(
            state = state,
            assistantName = settingsState.assistantName
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Central Animated Reactor Orb
        JarvisCoreOrb(
            state = state,
            audioAmplitude = amplitude,
            onClick = onOrbClick,
            size = 230.dp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Speech subtitle / real-time transcription or status prompt
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(44.dp),
            contentAlignment = Alignment.Center
        ) {
            if (partialText.isNotEmpty()) {
                Text(
                    text = "\"$partialText\"",
                    color = JarvisCyanLight,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                val promptText = when (state) {
                    is AssistantState.Listening -> "Listening to your voice... Speak now"
                    is AssistantState.Thinking -> "Analyzing query with ${settingsState.selectedModel}..."
                    is AssistantState.Speaking -> "Transmitting vocal response..."
                    is AssistantState.Error -> state.message
                    AssistantState.Idle -> "Tap reactor orb or speak a command"
                }
                Text(
                    text = promptText,
                    color = if (state is AssistantState.Error) Color(0xFFFF5555) else JarvisTextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = if (state is AssistantState.Thinking) FontFamily.Monospace else FontFamily.Default
                )
            }
        }

        // Live Audio Wave Visualizer
        VoiceWaveVisualizer(
            state = state,
            amplitude = amplitude,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Action Chips
        Text(
            text = "QUICK PROTOCOLS",
            color = JarvisCyan.copy(alpha = 0.5f),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
        QuickActionChips(
            onActionClick = { command ->
                onSendQuery(command)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Recent Response Preview Card
        if (lastAssistantMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JarvisCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LATEST RESPONSE",
                            color = JarvisCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            IconButton(
                                onClick = { onSpeakMessage(lastAssistantMessage.text) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Read Aloud",
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = onNavigateToChat,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "View History",
                                    tint = JarvisTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = lastAssistantMessage.text,
                        color = JarvisTextPrimary,
                        fontSize = 13.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom Fast Typing Console
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(JarvisCardBg, RoundedCornerShape(24.dp))
                .border(1.dp, JarvisCyan.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Type command or query...", color = JarvisTextMuted, fontSize = 13.sp)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = JarvisCyan
                ),
                maxLines = 2,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank()) {
                            onSendQuery(inputText)
                            inputText = ""
                            keyboardController?.hide()
                        }
                    }
                )
            )

            if (inputText.isNotBlank()) {
                IconButton(
                    onClick = {
                        onSendQuery(inputText)
                        inputText = ""
                        keyboardController?.hide()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(JarvisCyan, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = JarvisBackground,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onOrbClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (state is AssistantState.Listening) Color(0xFFFF4444) else JarvisNeonBlue.copy(alpha = 0.25f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (state is AssistantState.Listening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Mic Toggle",
                        tint = if (state is AssistantState.Listening) Color.White else JarvisCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
