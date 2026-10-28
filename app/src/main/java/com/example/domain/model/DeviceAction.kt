package com.example.domain.model

sealed interface DeviceAction {
    data class OpenApp(val appName: String, val packageName: String? = null) : DeviceAction
    data class SearchWeb(val query: String) : DeviceAction
    data class SetAlarm(val hour: Int, val minute: Int, val label: String) : DeviceAction
    data class SetTimer(val durationSeconds: Int, val label: String) : DeviceAction
    data class LaunchMaps(val location: String) : DeviceAction
    data class ToggleFlashlight(val enable: Boolean) : DeviceAction
    data class StoreMemory(val title: String, val content: String) : DeviceAction
    
    // Sensitive actions requiring confirmation
    data class DialPhone(val numberOrName: String) : DeviceAction
    data class ComposeMessage(val recipient: String, val text: String) : DeviceAction
    data class ClearHistory(val target: String) : DeviceAction
    
    data class GeneralResponse(val text: String) : DeviceAction
}

data class PendingAction(
    val action: DeviceAction,
    val title: String,
    val description: String,
    val warningText: String? = null
)
