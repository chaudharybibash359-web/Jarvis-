package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class SettingsPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("jarvis_settings_prefs", Context.MODE_PRIVATE)

    var customApiKey: String
        get() = prefs.getString("custom_api_key", "") ?: ""
        set(value) = prefs.edit().putString("custom_api_key", value.trim()).apply()

    var selectedModel: String
        get() = prefs.getString("selected_model", "gemini-3.5-flash") ?: "gemini-3.5-flash"
        set(value) = prefs.edit().putString("selected_model", value).apply()

    var assistantName: String
        get() = prefs.getString("assistant_name", "JARVIS") ?: "JARVIS"
        set(value) = prefs.edit().putString("assistant_name", value).apply()

    var userHonorific: String
        get() = prefs.getString("user_honorific", "Sir") ?: "Sir"
        set(value) = prefs.edit().putString("user_honorific", value).apply()

    var personality: String
        get() = prefs.getString("personality", "CLASSIC_JARVIS") ?: "CLASSIC_JARVIS"
        set(value) = prefs.edit().putString("personality", value).apply()

    var voicePitch: Float
        get() = prefs.getFloat("voice_pitch", 0.95f)
        set(value) = prefs.edit().putFloat("voice_pitch", value).apply()

    var voiceRate: Float
        get() = prefs.getFloat("voice_rate", 1.05f)
        set(value) = prefs.edit().putFloat("voice_rate", value).apply()

    var isContinuousMode: Boolean
        get() = prefs.getBoolean("continuous_mode", false)
        set(value) = prefs.edit().putBoolean("continuous_mode", value).apply()

    var isTtsAudioEnabled: Boolean
        get() = prefs.getBoolean("tts_enabled", true)
        set(value) = prefs.edit().putBoolean("tts_enabled", value).apply()

    var visualizerStyle: String
        get() = prefs.getString("visualizer_style", "PULSE") ?: "PULSE"
        set(value) = prefs.edit().putString("visualizer_style", value).apply()
}
