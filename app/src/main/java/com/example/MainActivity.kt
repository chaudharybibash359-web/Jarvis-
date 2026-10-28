package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.JarvisViewModel
import com.example.ui.components.ActionConfirmationDialog
import com.example.ui.screens.ConversationScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                JarvisAppContent()
            }
        }
    }
}

@Composable
fun JarvisAppContent(viewModel: JarvisViewModel = viewModel()) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val assistantState by viewModel.assistantState.collectAsState()
    val audioAmplitude by viewModel.audioAmplitude.collectAsState()
    val partialText by viewModel.partialText.collectAsState()
    val recentMessages by viewModel.recentMessages.collectAsState()
    val allMessages by viewModel.allMessages.collectAsState()
    val allMemories by viewModel.allMemories.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()
    val pendingAction by viewModel.pendingAction.collectAsState()

    // Runtime Permission Launcher for Record Audio
    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        } else {
            Toast.makeText(context, "Microphone permission is required for voice recognition.", Toast.LENGTH_LONG).show()
        }
    }

    val onMicAction = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.toggleVoiceInteraction()
        } else {
            recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = JarvisBackground,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .height(68.dp)
                    .background(JarvisCardBg)
                    .border(0.75.dp, JarvisCyan.copy(alpha = 0.2f)),
                containerColor = JarvisCardBg
            ) {
                // Tab 0: Core Orb
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Core HUD",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "CORE",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JarvisCyanLight,
                        selectedTextColor = JarvisCyan,
                        indicatorColor = JarvisCyan.copy(alpha = 0.15f),
                        unselectedIconColor = JarvisTextMuted,
                        unselectedTextColor = JarvisTextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_core")
                )

                // Tab 1: Transmissions / Conversation
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = "Chat Log",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "LOG",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JarvisCyanLight,
                        selectedTextColor = JarvisCyan,
                        indicatorColor = JarvisCyan.copy(alpha = 0.15f),
                        unselectedIconColor = JarvisTextMuted,
                        unselectedTextColor = JarvisTextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_log")
                )

                // Tab 2: Neural Memory
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Memory Bank",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "MEMORY",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JarvisCyanLight,
                        selectedTextColor = JarvisCyan,
                        indicatorColor = JarvisCyan.copy(alpha = 0.15f),
                        unselectedIconColor = JarvisTextMuted,
                        unselectedTextColor = JarvisTextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_memory")
                )

                // Tab 3: Settings
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "CONFIG",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JarvisCyanLight,
                        selectedTextColor = JarvisCyan,
                        indicatorColor = JarvisCyan.copy(alpha = 0.15f),
                        unselectedIconColor = JarvisTextMuted,
                        unselectedTextColor = JarvisTextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_config")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    state = assistantState,
                    amplitude = audioAmplitude,
                    partialText = partialText,
                    recentMessages = recentMessages,
                    settingsState = settingsState,
                    onOrbClick = onMicAction,
                    onSendQuery = { viewModel.sendTextQuery(it) },
                    onSpeakMessage = { viewModel.speakMessage(it) },
                    onNavigateToChat = { selectedTab = 1 }
                )
                1 -> ConversationScreen(
                    messages = allMessages,
                    state = assistantState,
                    onSendMessage = { viewModel.sendTextQuery(it) },
                    onSpeakMessage = { viewModel.speakMessage(it) },
                    onDeleteMessage = { viewModel.deleteMessage(it) },
                    onClearAll = { viewModel.clearHistory() },
                    onToggleMic = onMicAction
                )
                2 -> MemoryScreen(
                    memories = allMemories,
                    onAddMemory = { title, content, category ->
                        viewModel.saveMemory(title, content, category)
                    },
                    onDeleteMemory = { viewModel.deleteMemory(it) },
                    onClearAll = { viewModel.clearAllMemories() }
                )
                3 -> SettingsScreen(
                    settingsState = settingsState,
                    onUpdateApiKey = { viewModel.updateApiKey(it) },
                    onUpdateSelectedModel = { viewModel.updateSelectedModel(it) },
                    onUpdatePersonality = { viewModel.updatePersonality(it) },
                    onUpdateUserHonorific = { viewModel.updateUserHonorific(it) },
                    onUpdateAssistantName = { viewModel.updateAssistantName(it) },
                    onUpdateVoicePitch = { viewModel.updateVoicePitch(it) },
                    onUpdateVoiceRate = { viewModel.updateVoiceRate(it) },
                    onUpdateContinuousMode = { viewModel.updateContinuousMode(it) },
                    onUpdateTtsEnabled = { viewModel.updateTtsEnabled(it) },
                    onTestVoice = { pitch, rate -> viewModel.testVoice(pitch, rate) },
                    onClearHistory = { viewModel.clearHistory() },
                    onClearMemories = { viewModel.clearAllMemories() }
                )
            }

            // Sensitive action confirmation modal HUD
            pendingAction?.let { pending ->
                ActionConfirmationDialog(
                    pendingAction = pending,
                    onConfirm = { viewModel.executePendingAction() },
                    onDismiss = { viewModel.dismissPendingAction() }
                )
            }
        }
    }
}
