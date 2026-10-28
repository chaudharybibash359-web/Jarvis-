package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.JarvisApp
import com.example.data.local.MemoryEntity
import com.example.data.local.MessageEntity
import com.example.domain.model.AssistantState
import com.example.domain.model.DeviceAction
import com.example.domain.model.PendingAction
import com.example.domain.model.Personality
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as JarvisApp
    private val repository = app.assistantRepository
    private val voiceManager = app.voiceRecognitionManager
    private val ttsManager = app.textToSpeechManager
    private val toolManager = app.deviceToolManager
    private val preferences = app.settingsPreferences

    private val _assistantState = MutableStateFlow<AssistantState>(AssistantState.Idle)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _pendingAction = MutableStateFlow<PendingAction?>(null)
    val pendingAction: StateFlow<PendingAction?> = _pendingAction.asStateFlow()

    val partialText: StateFlow<String> = voiceManager.partialText

    // Combined amplitude from either microphone (while listening) or TTS (while speaking)
    val audioAmplitude: StateFlow<Float> = combine(
        voiceManager.rmsAmplitude,
        ttsManager.speechAmplitude,
        _assistantState
    ) { micAmp, ttsAmp, state ->
        when (state) {
            is AssistantState.Listening -> micAmp
            is AssistantState.Speaking -> ttsAmp
            is AssistantState.Thinking -> 0.45f
            else -> 0f
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val allMessages: StateFlow<List<MessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentMessages: StateFlow<List<MessageEntity>> = repository.recentMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMemories: StateFlow<List<MemoryEntity>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings observable states
    private val _settingsState = MutableStateFlow(loadSettings())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    data class SettingsState(
        val assistantName: String,
        val userHonorific: String,
        val personality: String,
        val customApiKey: String,
        val selectedModel: String,
        val voicePitch: Float,
        val voiceRate: Float,
        val isContinuous: Boolean,
        val isTtsAudioEnabled: Boolean
    )

    private fun loadSettings(): SettingsState {
        return SettingsState(
            assistantName = preferences.assistantName,
            userHonorific = preferences.userHonorific,
            personality = preferences.personality,
            customApiKey = preferences.customApiKey,
            selectedModel = preferences.selectedModel,
            voicePitch = preferences.voicePitch,
            voiceRate = preferences.voiceRate,
            isContinuous = preferences.isContinuousMode,
            isTtsAudioEnabled = preferences.isTtsAudioEnabled
        )
    }

    fun toggleVoiceInteraction() {
        when (_assistantState.value) {
            is AssistantState.Listening -> stopListening()
            is AssistantState.Speaking -> stopSpeaking()
            is AssistantState.Thinking -> {
                // Cancel thinking
                _assistantState.value = AssistantState.Idle
            }
            else -> startListening()
        }
    }

    fun startListening() {
        stopSpeaking()
        _assistantState.value = AssistantState.Listening()

        voiceManager.startListening(
            onResult = { spokenText ->
                processUserSpeech(spokenText)
            },
            onError = { errorMsg ->
                _assistantState.value = AssistantState.Error(errorMsg)
            }
        )
    }

    fun stopListening() {
        voiceManager.stopListening()
        if (_assistantState.value is AssistantState.Listening) {
            _assistantState.value = AssistantState.Idle
        }
    }

    fun stopSpeaking() {
        ttsManager.stop()
        if (_assistantState.value is AssistantState.Speaking) {
            _assistantState.value = AssistantState.Idle
        }
    }

    fun sendTextQuery(query: String) {
        if (query.isBlank()) return
        stopSpeaking()
        stopListening()
        processUserSpeech(query.trim())
    }

    private fun processUserSpeech(userText: String) {
        viewModelScope.launch {
            // Save user message to database
            repository.saveMessage(text = userText, isUser = true)
            _assistantState.value = AssistantState.Thinking(userText)

            val recentList = recentMessages.value
            val result = repository.processUserQuery(userText, recentList)

            // Check if action is sensitive and requires confirmation
            when (val action = result.deviceAction) {
                is DeviceAction.DialPhone -> {
                    _pendingAction.value = PendingAction(
                        action = action,
                        title = "Outgoing Call Confirmation",
                        description = "Initiate phone call to ${action.numberOrName}?",
                        warningText = "This will open the device dialer to place a call."
                    )
                    handleAssistantResponse(result.speechResponse, action)
                }
                is DeviceAction.ComposeMessage -> {
                    _pendingAction.value = PendingAction(
                        action = action,
                        title = "Send Message Confirmation",
                        description = "Draft dispatch to ${action.recipient} with text: '${action.text}'?",
                        warningText = "Requires confirmation to open messaging app."
                    )
                    handleAssistantResponse(result.speechResponse, action)
                }
                is DeviceAction.ClearHistory -> {
                    _pendingAction.value = PendingAction(
                        action = action,
                        title = "Data Purge Confirmation",
                        description = "Clear all conversation and memory databanks?",
                        warningText = "This action is irreversible."
                    )
                    handleAssistantResponse(result.speechResponse, action)
                }
                else -> {
                    // Safe direct execution
                    toolManager.executeAction(action)
                    handleAssistantResponse(result.speechResponse, action)
                }
            }
        }
    }

    private fun handleAssistantResponse(speechText: String, action: DeviceAction) {
        viewModelScope.launch {
            // Save assistant message to database
            repository.saveMessage(
                text = speechText,
                isUser = false,
                actionType = action::class.simpleName
            )

            _assistantState.value = AssistantState.Speaking(speechText)

            if (preferences.isTtsAudioEnabled) {
                ttsManager.speak(
                    text = speechText,
                    pitch = preferences.voicePitch,
                    rate = preferences.voiceRate,
                    onDone = {
                        viewModelScope.launch {
                            _assistantState.value = AssistantState.Idle
                            // Continuous conversation mode
                            if (preferences.isContinuousMode) {
                                startListening()
                            }
                        }
                    }
                )
            } else {
                _assistantState.value = AssistantState.Idle
            }
        }
    }

    fun executePendingAction() {
        val pending = _pendingAction.value ?: return
        _pendingAction.value = null
        viewModelScope.launch {
            val feedback = toolManager.executeAction(pending.action)
            speakFeedback(feedback)
        }
    }

    fun dismissPendingAction() {
        _pendingAction.value = null
        speakFeedback("Action aborted, ${preferences.userHonorific}.")
    }

    fun speakFeedback(text: String) {
        viewModelScope.launch {
            _assistantState.value = AssistantState.Speaking(text)
            ttsManager.speak(
                text = text,
                pitch = preferences.voicePitch,
                rate = preferences.voiceRate,
                onDone = {
                    _assistantState.value = AssistantState.Idle
                }
            )
        }
    }

    fun speakMessage(text: String) {
        stopSpeaking()
        _assistantState.value = AssistantState.Speaking(text)
        ttsManager.speak(
            text = text,
            pitch = preferences.voicePitch,
            rate = preferences.voiceRate,
            onDone = {
                _assistantState.value = AssistantState.Idle
            }
        )
    }

    fun saveMemory(title: String, content: String, category: String = "manual") {
        viewModelScope.launch {
            repository.saveMemory(title, content, category)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearAllMemories()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }

    fun updateApiKey(newKey: String) {
        preferences.customApiKey = newKey
        _settingsState.value = loadSettings()
    }

    fun updateSelectedModel(model: String) {
        preferences.selectedModel = model
        _settingsState.value = loadSettings()
    }

    fun updatePersonality(personality: Personality) {
        preferences.personality = personality.id
        _settingsState.value = loadSettings()
    }

    fun updateUserHonorific(honorific: String) {
        preferences.userHonorific = honorific
        _settingsState.value = loadSettings()
    }

    fun updateAssistantName(name: String) {
        preferences.assistantName = name
        _settingsState.value = loadSettings()
    }

    fun updateVoicePitch(pitch: Float) {
        preferences.voicePitch = pitch
        _settingsState.value = loadSettings()
    }

    fun updateVoiceRate(rate: Float) {
        preferences.voiceRate = rate
        _settingsState.value = loadSettings()
    }

    fun updateContinuousMode(enabled: Boolean) {
        preferences.isContinuousMode = enabled
        _settingsState.value = loadSettings()
    }

    fun updateTtsEnabled(enabled: Boolean) {
        preferences.isTtsAudioEnabled = enabled
        _settingsState.value = loadSettings()
    }

    fun testVoice(pitch: Float, rate: Float) {
        val testPhrase = "Greetings ${preferences.userHonorific}. Acoustic synthesizer modulation test nominal."
        stopSpeaking()
        _assistantState.value = AssistantState.Speaking(testPhrase)
        ttsManager.speak(
            text = testPhrase,
            pitch = pitch,
            rate = rate,
            onDone = {
                _assistantState.value = AssistantState.Idle
            }
        )
    }
}
