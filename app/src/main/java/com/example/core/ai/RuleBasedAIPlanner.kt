package com.example.core.ai

import com.example.core.automation.AppCapabilityManager
import com.example.core.model.ActionStep
import com.example.core.model.ActionType
import com.example.core.model.TaskPlan
import java.util.regex.Pattern

object RuleBasedAIPlanner {

    fun planFromQuery(rawQuery: String): AIResponse? {
        val q = rawQuery.trim()
        val lower = q.lowercase()

        // 1. Interruption & Cancellation
        if (lower in listOf("stop", "cancel", "never mind", "nevermind", "wait", "abort", "quiet", "chup", "ruko")) {
            return AIResponse(
                spokenResponse = "Cancelled.",
                taskPlan = null,
                isAction = false
            )
        }

        // 2. Greetings & Status Check
        if (lower in listOf("hello", "hi jarvis", "hey jarvis", "jarvis", "are you there", "kaisa hai", "status", "system status", "diagnostics")) {
            return AIResponse(
                spokenResponse = "Online and fully operational. How may I assist you?",
                taskPlan = TaskPlan(
                    userQuery = q,
                    intent = "SYSTEM_STATUS",
                    steps = listOf(
                        ActionStep(
                            type = ActionType.DEVICE_INFO,
                            title = "Diagnostic Check",
                            description = "Inspect system resources & connectivity"
                        )
                    )
                ),
                isAction = true
            )
        }

        // 3. App Launching ("Open YouTube", "Launch Spotify", "Instagram kholo", "Chrome open karo")
        val openAppPattern = Pattern.compile("^(?:open|launch|start|kholo|chalao)\\s+(.+)$", Pattern.CASE_INSENSITIVE)
        val openAppMatch = openAppPattern.matcher(lower)
        if (openAppMatch.matches()) {
            val appTarget = openAppMatch.group(1)?.trim() ?: ""
            val profile = AppCapabilityManager.findApp(appTarget)
            if (profile != null) {
                return AIResponse(
                    spokenResponse = "Opening ${profile.name}.",
                    taskPlan = TaskPlan(
                        userQuery = q,
                        intent = "OPEN_APP",
                        steps = listOf(
                            ActionStep(
                                type = ActionType.OPEN_APP,
                                title = "Launch ${profile.name}",
                                description = "Opening package ${profile.packageName}",
                                params = mapOf("package" to profile.packageName, "appName" to profile.name)
                            )
                        )
                    ),
                    isAction = true
                )
            }
        }

        // 4. App Search ("Search YouTube for flutter", "Open YouTube and search for tech news", "Search maps for coffee")
        val ytSearchPattern = Pattern.compile("(?:search\\s+youtube\\s+(?:for|about)?\\s*|open\\s+youtube\\s+and\\s+search\\s+(?:for)?\\s*)(.+)", Pattern.CASE_INSENSITIVE)
        val ytMatch = ytSearchPattern.matcher(lower)
        if (ytMatch.find()) {
            val queryParam = ytMatch.group(1)?.trim() ?: ""
            return AIResponse(
                spokenResponse = "Searching YouTube for $queryParam.",
                taskPlan = TaskPlan(
                    userQuery = q,
                    intent = "SEARCH_YOUTUBE",
                    steps = listOf(
                        ActionStep(
                            type = ActionType.SEARCH_APP,
                            title = "Search YouTube",
                            description = "Querying: $queryParam",
                            params = mapOf("appId" to "youtube", "query" to queryParam)
                        )
                    )
                ),
                isAction = true
            )
        }

        // Maps Search ("Search maps for pharmacy", "Find nearby restaurant", "Directions to hospital")
        val mapsPattern = Pattern.compile("(?:search\\s+maps\\s+(?:for)?|find\\s+nearby|directions\\s+to|navigate\\s+to)\\s+(.+)", Pattern.CASE_INSENSITIVE)
        val mapsMatch = mapsPattern.matcher(lower)
        if (mapsMatch.find()) {
            val place = mapsMatch.group(1)?.trim() ?: ""
            return AIResponse(
                spokenResponse = "Searching Maps for $place.",
                taskPlan = TaskPlan(
                    userQuery = q,
                    intent = "SEARCH_MAPS",
                    steps = listOf(
                        ActionStep(
                            type = ActionType.SEARCH_APP,
                            title = "Search Maps",
                            description = "Locating: $place",
                            params = mapOf("appId" to "maps", "query" to place)
                        )
                    )
                ),
                isAction = true
            )
        }

        // Spotify Search ("Play my music", "Search spotify for taylor swift", "Play lo-fi songs")
        val musicPattern = Pattern.compile("(?:play|search\\s+spotify\\s+(?:for)?)\\s+(.+)", Pattern.CASE_INSENSITIVE)
        val musicMatch = musicPattern.matcher(lower)
        if (musicMatch.find()) {
            val song = musicMatch.group(1)?.trim() ?: ""
            return AIResponse(
                spokenResponse = "Playing $song on Spotify.",
                taskPlan = TaskPlan(
                    userQuery = q,
                    intent = "SEARCH_SPOTIFY",
                    steps = listOf(
                        ActionStep(
                            type = ActionType.SEARCH_APP,
                            title = "Play Music",
                            description = "Playing: $song",
                            params = mapOf("appId" to "spotify", "query" to song)
                        )
                    )
                ),
                isAction = true
            )
        }

        // 5. Volume Control ("Set volume to 80%", "Increase volume", "Decrease volume", "Mute")
        if (lower.contains("volume") || lower.contains("sound") || lower.contains("awaz")) {
            var targetPercent = 70
            if (lower.contains("max") || lower.contains("full") || lower.contains("100")) {
                targetPercent = 100
            } else if (lower.contains("mute") || lower.contains("zero") || lower.contains("0")) {
                targetPercent = 0
            } else if (lower.contains("increase") || lower.contains("up") || lower.contains("badhao")) {
                targetPercent = 85
            } else if (lower.contains("decrease") || lower.contains("down") || lower.contains("lower") || lower.contains("kam")) {
                targetPercent = 35
            } else {
                val numPattern = Pattern.compile("(\\d{1,3})\\s*%?")
                val numMatch = numPattern.matcher(lower)
                if (numMatch.find()) {
                    targetPercent = (numMatch.group(1)?.toIntOrNull() ?: 70).coerceIn(0, 100)
                }
            }

            return AIResponse(
                spokenResponse = "Volume set to $targetPercent percent.",
                taskPlan = TaskPlan(
                    userQuery = q,
                    intent = "SET_VOLUME",
                    steps = listOf(
                        ActionStep(
                            type = ActionType.CONTROL_VOLUME,
                            title = "Adjust Volume",
                            description = "Set level to $targetPercent%",
                            params = mapOf("percent" to targetPercent.toString())
                        )
                    )
                ),
                isAction = true
            )
        }

        // 6. Flashlight ("Turn on flashlight", "Torch on", "Torch band karo", "Flashlight off")
        if (lower.contains("flashlight") || lower.contains("torch")) {
            val enable = !lower.contains("off") && !lower.contains("band") && !lower.contains("disable")
            return AIResponse(
                spokenResponse = if (enable) "Flashlight activated." else "Flashlight turned off.",
                taskPlan = TaskPlan(
                    userQuery = q,
                    intent = "TOGGLE_FLASHLIGHT",
                    steps = listOf(
                        ActionStep(
                            type = ActionType.FLASHLIGHT_CONTROL,
                            title = if (enable) "Turn On Flashlight" else "Turn Off Flashlight",
                            description = if (enable) "Activating device torch" else "Deactivating device torch",
                            params = mapOf("enable" to enable.toString())
                        )
                    )
                ),
                isAction = true
            )
        }

        // 7. Alarms & Timers ("Set an alarm for 7 AM", "Set a timer for 15 minutes")
        if (lower.contains("alarm")) {
            var hour = 7
            var min = 0
            val isPm = lower.contains("pm") || lower.contains("evening") || lower.contains("night") || lower.contains("shaam")
            val timeMatch = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?").matcher(lower)
            if (timeMatch.find()) {
                hour = timeMatch.group(1)?.toIntOrNull() ?: 7
                min = timeMatch.group(2)?.toIntOrNull() ?: 0
                if (isPm && hour < 12) hour += 12
            }
            val formattedTime = String.format("%02d:%02d", hour, min)
            return AIResponse(
                spokenResponse = "Setting alarm for $formattedTime.",
                taskPlan = TaskPlan(
                    userQuery = q,
                    intent = "SET_ALARM",
                    steps = listOf(
                        ActionStep(
                            type = ActionType.SET_ALARM,
                            title = "Create Alarm",
                            description = "Alarm set for $formattedTime",
                            params = mapOf("hour" to hour.toString(), "minutes" to min.toString(), "message" to "JARVIS Alarm")
                        )
                    )
                ),
                isAction = true
            )
        }

        if (lower.contains("timer")) {
            var seconds = 600 // default 10 min
            val minMatch = Pattern.compile("(\\d+)\\s*(?:min|minute|minutes|m)").matcher(lower)
            val secMatch = Pattern.compile("(\\d+)\\s*(?:sec|second|seconds|s)").matcher(lower)
            if (minMatch.find()) {
                val mins = minMatch.group(1)?.toIntOrNull() ?: 10
                seconds = mins * 60
            } else if (secMatch.find()) {
                seconds = secMatch.group(1)?.toIntOrNull() ?: 60
            }

            val minDisplay = seconds / 60
            return AIResponse(
                spokenResponse = "Timer started for $minDisplay minutes.",
                taskPlan = TaskPlan(
                    userQuery = q,
                    intent = "SET_TIMER",
                    steps = listOf(
                        ActionStep(
                            type = ActionType.SET_TIMER,
                            title = "Start Timer",
                            description = "Duration: $minDisplay min",
                            params = mapOf("seconds" to seconds.toString(), "message" to "JARVIS Timer")
                        )
                    )
                ),
                isAction = true
            )
        }

        // 8. Communication: Calling & Messaging ("Call Mom", "Send Rahul a message saying I will be home soon")
        val callPattern = Pattern.compile("(?:call|phone|dial|phone lagao)\\s+(.+)", Pattern.CASE_INSENSITIVE)
        val callMatch = callPattern.matcher(lower)
        if (callMatch.find()) {
            val target = callMatch.group(1)?.trim() ?: ""
            return AIResponse(
                spokenResponse = "Preparing to call $target.",
                taskPlan = TaskPlan(
                    userQuery = q,
                    intent = "MAKE_CALL",
                    steps = listOf(
                        ActionStep(
                            type = ActionType.MAKE_CALL,
                            title = "Call $target",
                            description = "Initiate call to $target",
                            params = mapOf("target" to target),
                            requiresConfirmation = true
                        )
                    )
                ),
                isAction = true
            )
        }

        val msgPattern = Pattern.compile("(?:send|message|text|draft)\\s+([a-zA-Z0-9_]+)\\s+(?:saying|that|:)?\\s*(.+)", Pattern.CASE_INSENSITIVE)
        val msgMatch = msgPattern.matcher(lower)
        if (msgMatch.find()) {
            val recipient = msgMatch.group(1)?.trim() ?: ""
            val messageText = msgMatch.group(2)?.trim() ?: ""
            return AIResponse(
                spokenResponse = "Drafting message for $recipient: \"$messageText\".",
                taskPlan = TaskPlan(
                    userQuery = q,
                    intent = "DRAFT_MESSAGE",
                    steps = listOf(
                        ActionStep(
                            type = ActionType.DRAFT_MESSAGE,
                            title = "Message $recipient",
                            description = "Text: $messageText",
                            params = mapOf("recipient" to recipient, "message" to messageText),
                            requiresConfirmation = true
                        )
                    )
                ),
                isAction = true
            )
        }

        // 9. Notifications ("Read my notifications", "What notifications did I get?", "Any messages?")
        if (lower.contains("notification") || lower.contains("notifications") || lower.contains("messages")) {
            return AIResponse(
                spokenResponse = "Checking your active notifications.",
                taskPlan = TaskPlan(
                    userQuery = q,
                    intent = "READ_NOTIFICATIONS",
                    steps = listOf(
                        ActionStep(
                            type = ActionType.READ_NOTIFICATIONS,
                            title = "Read Notifications",
                            description = "Extract and summarize incoming alerts"
                        )
                    )
                ),
                isAction = true
            )
        }

        // 10. Notes & Reminders ("Create a note saying...", "Remind me to study at 6 PM")
        if (lower.contains("note") || lower.contains("remember that") || lower.contains("take a note")) {
            val content = q.replace(Regex("(?i)^(create a note|take a note|add a note|remember that|note saying)\\s*"), "").trim()
            return AIResponse(
                spokenResponse = "Note recorded.",
                taskPlan = TaskPlan(
                    userQuery = q,
                    intent = "CREATE_NOTE",
                    steps = listOf(
                        ActionStep(
                            type = ActionType.CREATE_NOTE,
                            title = "Save Note",
                            description = "Content: $content",
                            params = mapOf("title" to "Voice Note", "content" to content)
                        )
                    )
                ),
                isAction = true
            )
        }

        if (lower.contains("remind me")) {
            val reminderText = q.replace(Regex("(?i)^remind me to\\s*"), "").trim()
            return AIResponse(
                spokenResponse = "Reminder saved.",
                taskPlan = TaskPlan(
                    userQuery = q,
                    intent = "CREATE_REMINDER",
                    steps = listOf(
                        ActionStep(
                            type = ActionType.CREATE_REMINDER,
                            title = "Add Reminder",
                            description = "Reminder: $reminderText",
                            params = mapOf("content" to reminderText)
                        )
                    )
                ),
                isAction = true
            )
        }

        // 11. Navigation Actions ("Go back", "Go home", "Take a screenshot", "Take screenshot")
        if (lower in listOf("go back", "back", "piche jao", "previous")) {
            return AIResponse(
                spokenResponse = "Navigating back.",
                taskPlan = TaskPlan(
                    userQuery = q,
                    intent = "PRESS_BACK",
                    steps = listOf(
                        ActionStep(type = ActionType.PRESS_BACK, title = "Back", description = "Perform global back action")
                    )
                ),
                isAction = true
            )
        }

        if (lower in listOf("go home", "home screen", "ghar jao")) {
            return AIResponse(
                spokenResponse = "Going home.",
                taskPlan = TaskPlan(
                    userQuery = q,
                    intent = "PRESS_HOME",
                    steps = listOf(
                        ActionStep(type = ActionType.PRESS_HOME, title = "Home", description = "Perform global home action")
                    )
                ),
                isAction = true
            )
        }

        if (lower.contains("screenshot")) {
            return AIResponse(
                spokenResponse = "Capturing screenshot.",
                taskPlan = TaskPlan(
                    userQuery = q,
                    intent = "TAKE_SCREENSHOT",
                    steps = listOf(
                        ActionStep(type = ActionType.TAKE_SCREENSHOT, title = "Screenshot", description = "Take screen capture via Accessibility")
                    )
                ),
                isAction = true
            )
        }

        // 12. Settings Shortcuts ("Open Bluetooth settings", "Open Wi-Fi settings", "Open settings")
        if (lower.contains("settings") || lower.contains("setting")) {
            val type = when {
                lower.contains("bluetooth") -> "bluetooth"
                lower.contains("wifi") || lower.contains("wi-fi") -> "wifi"
                lower.contains("display") || lower.contains("brightness") -> "display"
                lower.contains("sound") -> "sound"
                lower.contains("battery") -> "battery"
                lower.contains("accessibility") -> "accessibility"
                else -> "general"
            }
            return AIResponse(
                spokenResponse = "Opening $type settings.",
                taskPlan = TaskPlan(
                    userQuery = q,
                    intent = "OPEN_SETTINGS",
                    steps = listOf(
                        ActionStep(
                            type = ActionType.OPEN_SETTINGS,
                            title = "Open Settings",
                            description = "Opening $type configuration",
                            params = mapOf("type" to type)
                        )
                    )
                ),
                isAction = true
            )
        }

        // 13. General Web Search ("Search the web for weather", "Google search python tutorials")
        val webSearchPattern = Pattern.compile("(?:search\\s+the\\s+web\\s+for|search\\s+google\\s+for|google\\s+search|search\\s+for)\\s+(.+)", Pattern.CASE_INSENSITIVE)
        val webMatch = webSearchPattern.matcher(lower)
        if (webMatch.find()) {
            val queryText = webMatch.group(1)?.trim() ?: ""
            return AIResponse(
                spokenResponse = "Searching the web for $queryText.",
                taskPlan = TaskPlan(
                    userQuery = q,
                    intent = "SEARCH_WEB",
                    steps = listOf(
                        ActionStep(
                            type = ActionType.SEARCH_WEB,
                            title = "Web Search",
                            description = "Query: $queryText",
                            params = mapOf("query" to queryText)
                        )
                    )
                ),
                isAction = true
            )
        }

        // No deterministic rule matched -> hand over to Gemini AI
        return null
    }
}
