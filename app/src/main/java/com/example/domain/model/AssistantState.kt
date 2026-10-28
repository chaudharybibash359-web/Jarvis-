package com.example.domain.model

sealed interface AssistantState {
    data object Idle : AssistantState
    data class Listening(val rmsDb: Float = 0f) : AssistantState
    data class Thinking(val query: String) : AssistantState
    data class Speaking(val text: String, val audioWaveLevel: Float = 0.5f) : AssistantState
    data class Error(val message: String, val canRetry: Boolean = true) : AssistantState
}
