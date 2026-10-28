package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Personality
import com.example.ui.JarvisViewModel
import com.example.ui.theme.JarvisAccentGreen
import com.example.ui.theme.JarvisAccentRed
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun SettingsScreen(
    settingsState: JarvisViewModel.SettingsState,
    onUpdateApiKey: (String) -> Unit,
    onUpdateSelectedModel: (String) -> Unit,
    onUpdatePersonality: (Personality) -> Unit,
    onUpdateUserHonorific: (String) -> Unit,
    onUpdateAssistantName: (String) -> Unit,
    onUpdateVoicePitch: (Float) -> Unit,
    onUpdateVoiceRate: (Float) -> Unit,
    onUpdateContinuousMode: (Boolean) -> Unit,
    onUpdateTtsEnabled: (Boolean) -> Unit,
    onTestVoice: (Float, Float) -> Unit,
    onClearHistory: () -> Unit,
    onClearMemories: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var apiKeyInput by remember(settingsState.customApiKey) { mutableStateOf(settingsState.customApiKey) }
    var isApiKeyVisible by remember { mutableStateOf(false) }
    var honorificInput by remember(settingsState.userHonorific) { mutableStateOf(settingsState.userHonorific) }
    var assistantNameInput by remember(settingsState.assistantName) { mutableStateOf(settingsState.assistantName) }

    var localPitch by remember(settingsState.voicePitch) { mutableFloatStateOf(settingsState.voicePitch) }
    var localRate by remember(settingsState.voiceRate) { mutableFloatStateOf(settingsState.voiceRate) }

    val models = listOf(
        "gemini-3.5-flash" to "Gemini 3.5 Flash (Recommended)",
        "gemini-3.1-pro-preview" to "Gemini 3.1 Pro (Deep Reasoning)",
        "gemini-3.1-flash-lite-preview" to "Gemini 3.1 Flash Lite (Ultra Fast)"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Header
        Text(
            text = "SYSTEM CONFIGURATION",
            color = JarvisCyan.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Settings & Preferences",
            color = JarvisTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section 1: AI Model & API Key
        SettingsSectionHeader(title = "NEURAL MODEL & SECURITY", icon = Icons.Default.Key)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Gemini API Key",
                    color = JarvisTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Custom key takes priority over built-in template key. Saved securely in local device storage.",
                    color = JarvisTextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("AIzaSy...", color = JarvisTextMuted, fontSize = 12.sp) },
                    visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                            Icon(
                                imageVector = if (isApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Key Visibility",
                                tint = JarvisCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisCyan.copy(alpha = 0.3f),
                        cursorColor = JarvisCyan
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = {
                            onUpdateApiKey(apiKeyInput.trim())
                            Toast.makeText(context, "API Key updated", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SAVE KEY", color = JarvisBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Active AI Model",
                    color = JarvisTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                models.forEach { (modelId, label) ->
                    val isSelected = settingsState.selectedModel == modelId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(
                                if (isSelected) JarvisCyan.copy(alpha = 0.12f) else JarvisSurfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) JarvisCyan else JarvisCyan.copy(alpha = 0.15f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onUpdateSelectedModel(modelId) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = modelId,
                                color = if (isSelected) JarvisCyanLight else JarvisTextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = label, color = JarvisTextSecondary, fontSize = 10.sp)
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = JarvisCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section 2: Persona & Identity
        SettingsSectionHeader(title = "PERSONALITY MATRIX & IDENTITY", icon = Icons.Default.Psychology)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Assistant Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = assistantNameInput,
                        onValueChange = {
                            assistantNameInput = it
                            onUpdateAssistantName(it)
                        },
                        label = { Text("Assistant Call Name", color = JarvisTextSecondary, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary,
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCyan.copy(alpha = 0.3f)
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = honorificInput,
                        onValueChange = {
                            honorificInput = it
                            onUpdateUserHonorific(it)
                        },
                        label = { Text("User Honorific (Title)", color = JarvisTextSecondary, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary,
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCyan.copy(alpha = 0.3f)
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Persona Archetype",
                    color = JarvisTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Personality.entries.forEach { p ->
                    val isSelected = settingsState.personality == p.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .background(
                                if (isSelected) JarvisCyan.copy(alpha = 0.12f) else JarvisSurfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) JarvisCyan else JarvisCyan.copy(alpha = 0.12f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onUpdatePersonality(p) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = p.displayName,
                                color = if (isSelected) JarvisCyanLight else JarvisTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = p.description,
                                color = JarvisTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = JarvisCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section 3: Speech Synthesizer & Voice
        SettingsSectionHeader(title = "ACOUSTICS & SYNTHESIZER", icon = Icons.Default.RecordVoiceOver)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Vocal response toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Vocal Audio Response",
                            color = JarvisTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Read aloud responses using TTS",
                            color = JarvisTextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = settingsState.isTtsAudioEnabled,
                        onCheckedChange = onUpdateTtsEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JarvisBackground,
                            checkedTrackColor = JarvisCyan
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Continuous conversation mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Continuous Conversation Mode",
                            color = JarvisTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Automatically listen again after speaking finishes",
                            color = JarvisTextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = settingsState.isContinuous,
                        onCheckedChange = onUpdateContinuousMode,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JarvisBackground,
                            checkedTrackColor = JarvisCyan
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pitch Slider
                Text(
                    text = "Synthesizer Pitch: ${String.format("%.2f", localPitch)}x",
                    color = JarvisTextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Slider(
                    value = localPitch,
                    onValueChange = {
                        localPitch = it
                        onUpdateVoicePitch(it)
                    },
                    valueRange = 0.6f..1.4f,
                    colors = SliderDefaults.colors(
                        thumbColor = JarvisCyan,
                        activeTrackColor = JarvisCyan
                    )
                )

                // Speed Rate Slider
                Text(
                    text = "Speech Speed Rate: ${String.format("%.2f", localRate)}x",
                    color = JarvisTextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Slider(
                    value = localRate,
                    onValueChange = {
                        localRate = it
                        onUpdateVoiceRate(it)
                    },
                    valueRange = 0.7f..1.6f,
                    colors = SliderDefaults.colors(
                        thumbColor = JarvisCyan,
                        activeTrackColor = JarvisCyan
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { onTestVoice(localPitch, localRate) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Test Voice",
                        tint = JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TEST VOICE SYNTHESIZER",
                        color = JarvisCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section 4: Privacy & Security Overview
        SettingsSectionHeader(title = "PRIVACY & LOCAL ARCHITECTURE", icon = Icons.Default.Security)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Edge vs Cloud Data Transparency",
                    color = JarvisTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Local on Device: Voice recording, speech recognition, device actions (camera/flashlight, alarms, opening apps, maps, phone dialer), and memory database are processed locally.\n• Cloud AI: Query text and conversation context are transmitted securely to Google Gemini API over HTTPS to generate answers.",
                    color = JarvisTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onClearHistory()
                            Toast.makeText(context, "Chat history purged", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisAccentRed.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Clear Chat",
                            tint = JarvisAccentRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PURGE CHAT", color = JarvisAccentRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            onClearMemories()
                            Toast.makeText(context, "Memory bank cleared", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisAccentRed.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Clear Memories",
                            tint = JarvisAccentRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PURGE MEMORY", color = JarvisAccentRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = JarvisCyan,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = title,
            color = JarvisCyan,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
    }
}
