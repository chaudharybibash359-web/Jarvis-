package com.example.data.repository

import com.example.data.local.MemoryDao
import com.example.data.local.MemoryEntity
import com.example.data.local.MessageDao
import com.example.data.local.MessageEntity
import com.example.data.local.SettingsPreferences
import com.example.data.remote.GeminiClient
import com.example.domain.model.DeviceAction
import com.example.domain.model.Personality
import com.example.domain.tools.IntentParser
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AssistantRepository(
    private val messageDao: MessageDao,
    private val memoryDao: MemoryDao,
    private val settingsPreferences: SettingsPreferences,
    private val geminiClient: GeminiClient = GeminiClient()
) {

    val allMessages: Flow<List<MessageEntity>> = messageDao.getAllMessages()
    val recentMessages: Flow<List<MessageEntity>> = messageDao.getRecentMessages(6)
    val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()

    suspend fun saveMessage(text: String, isUser: Boolean, actionType: String? = null, actionDetail: String? = null): Long {
        return messageDao.insertMessage(
            MessageEntity(
                text = text,
                isUser = isUser,
                actionType = actionType,
                actionDetail = actionDetail
            )
        )
    }

    suspend fun deleteMessage(id: Long) {
        messageDao.deleteMessageById(id)
    }

    suspend fun clearHistory() {
        messageDao.clearAllMessages()
    }

    suspend fun saveMemory(title: String, content: String, category: String = "general"): Long {
        return memoryDao.insertMemory(
            MemoryEntity(
                title = title,
                content = content,
                category = category
            )
        )
    }

    suspend fun deleteMemory(id: Long) {
        memoryDao.deleteMemoryById(id)
    }

    suspend fun clearAllMemories() {
        memoryDao.clearAllMemories()
    }

    suspend fun processUserQuery(
        userQuery: String,
        recentChatList: List<MessageEntity>
    ): ProcessResult {
        val honorific = settingsPreferences.userHonorific
        val personality = Personality.fromId(settingsPreferences.personality)
        val customKey = settingsPreferences.customApiKey
        val selectedModel = settingsPreferences.selectedModel

        // 1. Check for immediate direct local device actions (Flashlight, Open App, Alarm, Timer)
        val directIntent = IntentParser.parseDirectIntent(userQuery)
        if (directIntent != null && directIntent !is DeviceAction.StoreMemory) {
            val speechReply = when (directIntent) {
                is DeviceAction.ToggleFlashlight -> if (directIntent.enable) "Activating illumination, $honorific." else "Powering down flashlight, $honorific."
                is DeviceAction.OpenApp -> "Accessing ${directIntent.appName}, $honorific."
                is DeviceAction.SetAlarm -> "Configuring alarm for ${String.format("%02d:%02d", directIntent.hour, directIntent.minute)}, $honorific."
                is DeviceAction.SetTimer -> "Setting timer, $honorific."
                is DeviceAction.LaunchMaps -> "Calculating transit vector to ${directIntent.location}, $honorific."
                is DeviceAction.SearchWeb -> "Querying global databanks for '${directIntent.query}', $honorific."
                is DeviceAction.DialPhone -> "Preparing communication channel for ${directIntent.numberOrName}."
                is DeviceAction.ComposeMessage -> "Preparing dispatch for ${directIntent.recipient}."
                else -> "Executing requested protocol, $honorific."
            }
            return ProcessResult(
                speechResponse = speechReply,
                deviceAction = directIntent
            )
        }

        // 2. Fetch stored memories for context
        val memoryList = try {
            memoryDao.getAllMemoriesList().map { "${it.title}: ${it.content}" }
        } catch (e: Exception) {
            emptyList()
        }

        // Check if query is explicitly asking to remember something
        if (directIntent is DeviceAction.StoreMemory) {
            saveMemory(directIntent.title, directIntent.content, "user_note")
            return ProcessResult(
                speechResponse = "I have committed that to permanent memory, $honorific.",
                deviceAction = directIntent
            )
        }

        // 3. Format conversation history for Gemini
        val historyTurns = recentChatList.map { Pair(it.text, it.isUser) }

        // 4. Call Gemini AI
        val geminiResult = geminiClient.generateResponse(
            userQuery = userQuery,
            conversationHistory = historyTurns,
            memories = memoryList,
            personality = personality,
            userHonorific = honorific,
            customApiKey = customKey,
            selectedModel = selectedModel
        )

        return if (geminiResult.isSuccess) {
            val rawAiText = geminiResult.getOrThrow()
            // Check for action tags in AI output
            val (extractedAction, cleanedText) = IntentParser.parseGeminiActionTag(rawAiText)
            
            // If action was a store memory action
            if (extractedAction is DeviceAction.StoreMemory) {
                saveMemory(extractedAction.title, extractedAction.content, "learned_preference")
            }

            ProcessResult(
                speechResponse = cleanedText,
                deviceAction = extractedAction ?: DeviceAction.GeneralResponse(cleanedText)
            )
        } else {
            val throwable = geminiResult.exceptionOrNull()
            // Provide intelligent local fallback response
            handleLocalFallback(userQuery, honorific, personality, throwable?.message)
        }
    }

    private fun handleLocalFallback(
        query: String,
        honorific: String,
        personality: Personality,
        errorMessage: String?
    ): ProcessResult {
        val lower = query.lowercase()

        // Local smart responses
        return when {
            lower.contains("time") -> {
                val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                ProcessResult(
                    speechResponse = "The current time is $time, $honorific.",
                    deviceAction = DeviceAction.GeneralResponse("Time check")
                )
            }
            lower.contains("date") || lower.contains("day") -> {
                val date = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
                ProcessResult(
                    speechResponse = "Today is $date, $honorific.",
                    deviceAction = DeviceAction.GeneralResponse("Date check")
                )
            }
            lower.contains("who are you") || lower.contains("what is your name") -> {
                ProcessResult(
                    speechResponse = "I am JARVIS, your personal artificial intelligence assistant. System diagnostics are fully operational.",
                    deviceAction = DeviceAction.GeneralResponse("Identity check")
                )
            }
            lower.contains("status") || lower.contains("diagnostic") -> {
                ProcessResult(
                    speechResponse = "All secondary subroutines online, $honorific. Neural voice processor active, local sensors nominal.",
                    deviceAction = DeviceAction.GeneralResponse("System status")
                )
            }
            lower.contains("hello") || lower.contains("hi jarvis") || lower.contains("hey jarvis") -> {
                val greeting = when (personality) {
                    Personality.CYBERPUNK -> "Neural link established. What's the mission, $honorific?"
                    Personality.TACTICAL -> "A.E.G.I.S. online and standing by for orders, $honorific."
                    Personality.CONCISE -> "Online. How can I assist?"
                    else -> "At your service, $honorific. How may I assist you today?"
                }
                ProcessResult(
                    speechResponse = greeting,
                    deviceAction = DeviceAction.GeneralResponse("Greeting")
                )
            }
            errorMessage == "NO_API_KEY" -> {
                ProcessResult(
                    speechResponse = "I am operating in local offline mode, $honorific. For full AI reasoning and dynamic answers, please configure your Gemini API key in the Settings tab.",
                    deviceAction = DeviceAction.GeneralResponse("No API key notice")
                )
            }
            else -> {
                ProcessResult(
                    speechResponse = "I am standing by, $honorific. I can open applications, launch navigation, set alarms, control illumination, or search the global network.",
                    deviceAction = DeviceAction.GeneralResponse("General fallback")
                )
            }
        }
    }

    data class ProcessResult(
        val speechResponse: String,
        val deviceAction: DeviceAction
    )
}
