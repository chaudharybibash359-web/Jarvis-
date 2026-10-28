package com.example.domain.tools

import com.example.domain.model.DeviceAction
import java.util.regex.Pattern

object IntentParser {

    fun parseDirectIntent(rawInput: String): DeviceAction? {
        val input = rawInput.trim().lowercase()

        // Flashlight / Torch
        if (input.contains("turn on flashlight") || input.contains("flashlight on") || input.contains("torch on") || input.contains("enable flashlight")) {
            return DeviceAction.ToggleFlashlight(true)
        }
        if (input.contains("turn off flashlight") || input.contains("flashlight off") || input.contains("torch off") || input.contains("disable flashlight")) {
            return DeviceAction.ToggleFlashlight(false)
        }

        // Open App: "open youtube", "launch camera", "start chrome", "open settings"
        val openAppRegex = Regex("""^(?:open|launch|start|run)\s+([a-zA-Z0-9\s]+)$""", RegexOption.IGNORE_CASE)
        val openMatch = openAppRegex.find(input)
        if (openMatch != null) {
            val appName = openMatch.groupValues[1].trim()
            if (appName.isNotEmpty() && !appName.startsWith("alarm") && !appName.startsWith("timer")) {
                return DeviceAction.OpenApp(appName)
            }
        }

        // Set Alarm: "set alarm for 7:30 am", "set an alarm at 8 am", "wake me up at 6:00"
        val alarmPattern = Pattern.compile("""(?:set\s+(?:an\s+)?alarm|wake\s+me\s+up)\s+(?:for|at)?\s*(\d{1,2})(?::(\d{2}))?\s*(am|pm)?""", Pattern.CASE_INSENSITIVE)
        val alarmMatcher = alarmPattern.matcher(input)
        if (alarmMatcher.find()) {
            var hour = alarmMatcher.group(1)?.toIntOrNull() ?: 7
            val min = alarmMatcher.group(2)?.toIntOrNull() ?: 0
            val amPm = alarmMatcher.group(3)?.lowercase()
            if (amPm == "pm" && hour < 12) hour += 12
            if (amPm == "am" && hour == 12) hour = 0
            return DeviceAction.SetAlarm(hour, min, "JARVIS Alarm")
        }

        // Timer: "set a timer for 5 minutes", "timer 30 seconds"
        val timerPattern = Pattern.compile("""(?:set\s+(?:a\s+)?timer\s+(?:for\s+)?|timer\s+)(\d+)\s*(minute|second|min|sec)s?""", Pattern.CASE_INSENSITIVE)
        val timerMatcher = timerPattern.matcher(input)
        if (timerMatcher.find()) {
            val amount = timerMatcher.group(1)?.toIntOrNull() ?: 1
            val unit = timerMatcher.group(2)?.lowercase() ?: "minute"
            val totalSeconds = if (unit.startsWith("min")) amount * 60 else amount
            return DeviceAction.SetTimer(totalSeconds, "JARVIS Timer")
        }

        // Search Web: "search for ...", "google ...", "search web for ..."
        val searchRegex = Regex("""^(?:search\s+(?:for\s+|the\s+web\s+for\s+)?|google\s+)(.+)$""", RegexOption.IGNORE_CASE)
        val searchMatch = searchRegex.find(input)
        if (searchMatch != null) {
            val query = searchMatch.groupValues[1].trim()
            return DeviceAction.SearchWeb(query)
        }

        // Maps: "navigate to ...", "directions to ...", "show map of ...", "find route to ..."
        val mapRegex = Regex("""^(?:navigate\s+to|directions\s+to|show\s+map\s+(?:of|for)|take\s+me\s+to)\s+(.+)$""", RegexOption.IGNORE_CASE)
        val mapMatch = mapRegex.find(input)
        if (mapMatch != null) {
            val dest = mapMatch.groupValues[1].trim()
            return DeviceAction.LaunchMaps(dest)
        }

        // Call: "call 555-1234", "call Mom", "dial 911"
        val callRegex = Regex("""^(?:call|dial)\s+(.+)$""", RegexOption.IGNORE_CASE)
        val callMatch = callRegex.find(input)
        if (callMatch != null) {
            val target = callMatch.groupValues[1].trim()
            return DeviceAction.DialPhone(target)
        }

        // Message: "send message to [name] saying [message]" or "text [name] [message]"
        val msgPattern = Pattern.compile("""(?:send\s+(?:a\s+)?message\s+to|text)\s+([a-zA-Z0-9\s]+?)\s+(?:saying|that|with)\s+(.+)$""", Pattern.CASE_INSENSITIVE)
        val msgMatcher = msgPattern.matcher(input)
        if (msgMatcher.find()) {
            val recipient = msgMatcher.group(1)?.trim() ?: ""
            val body = msgMatcher.group(2)?.trim() ?: ""
            if (recipient.isNotBlank() && body.isNotBlank()) {
                return DeviceAction.ComposeMessage(recipient, body)
            }
        }

        // Memory: "remember that ...", "take a note ...", "save note ..."
        val memoryRegex = Regex("""^(?:remember\s+(?:that\s+)?|take\s+a\s+note\s+(?:that\s+)?|save\s+(?:a\s+)?note\s+)(.+)$""", RegexOption.IGNORE_CASE)
        val memMatch = memoryRegex.find(input)
        if (memMatch != null) {
            val note = memMatch.groupValues[1].trim()
            return DeviceAction.StoreMemory(title = "Note", content = note)
        }

        return null
    }

    /**
     * Parses special [ACTION: ...] tags from Gemini responses if emitted
     */
    fun parseGeminiActionTag(aiResponse: String): Pair<DeviceAction?, String> {
        val tagRegex = Regex("""\[ACTION:\s*([A-Z_]+)(?::\s*([^\]]+))?\]""")
        val match = tagRegex.find(aiResponse) ?: return Pair(null, aiResponse)

        val actionName = match.groupValues[1].trim()
        val param = match.groupValues[2].trim()
        val cleanText = aiResponse.replace(match.value, "").trim()

        val action = when (actionName) {
            "OPEN_APP" -> DeviceAction.OpenApp(param)
            "SEARCH" -> DeviceAction.SearchWeb(param)
            "MAPS" -> DeviceAction.LaunchMaps(param)
            "FLASHLIGHT" -> DeviceAction.ToggleFlashlight(param.equals("ON", ignoreCase = true))
            "CALL" -> DeviceAction.DialPhone(param)
            "MESSAGE" -> {
                val parts = param.split(":", limit = 2)
                if (parts.size >= 2) DeviceAction.ComposeMessage(parts[0].trim(), parts[1].trim())
                else DeviceAction.ComposeMessage(param, "")
            }
            "REMEMBER" -> DeviceAction.StoreMemory("Saved Information", param)
            else -> null
        }

        return Pair(action, cleanText)
    }
}
