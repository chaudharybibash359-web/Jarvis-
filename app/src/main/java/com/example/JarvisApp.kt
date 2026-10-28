package com.example

import android.app.Application
import com.example.data.local.JarvisDatabase
import com.example.data.local.SettingsPreferences
import com.example.data.repository.AssistantRepository
import com.example.domain.tools.DeviceToolManager
import com.example.voice.TextToSpeechManager
import com.example.voice.VoiceRecognitionManager

class JarvisApp : Application() {

    lateinit var database: JarvisDatabase
        private set

    lateinit var settingsPreferences: SettingsPreferences
        private set

    lateinit var voiceRecognitionManager: VoiceRecognitionManager
        private set

    lateinit var textToSpeechManager: TextToSpeechManager
        private set

    lateinit var deviceToolManager: DeviceToolManager
        private set

    lateinit var assistantRepository: AssistantRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = JarvisDatabase.getDatabase(this)
        settingsPreferences = SettingsPreferences(this)
        voiceRecognitionManager = VoiceRecognitionManager(this)
        textToSpeechManager = TextToSpeechManager(this)
        deviceToolManager = DeviceToolManager(this)

        assistantRepository = AssistantRepository(
            messageDao = database.messageDao(),
            memoryDao = database.memoryDao(),
            settingsPreferences = settingsPreferences
        )
    }

    override fun onTerminate() {
        super.onTerminate()
        textToSpeechManager.shutdown()
        voiceRecognitionManager.stopListening()
    }
}
