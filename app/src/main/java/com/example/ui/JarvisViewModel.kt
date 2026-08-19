package com.example.ui

import android.app.Application
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.JarvisApplication
import com.example.core.ai.AIResponse
import com.example.core.ai.GeminiAIProvider
import com.example.core.ai.HybridAIPlanner
import com.example.core.automation.AccessibilityController
import com.example.core.automation.CommunicationController
import com.example.core.automation.DeviceController
import com.example.core.database.ConversationEntity
import com.example.core.database.JarvisRepository
import com.example.core.database.NoteEntity
import com.example.core.database.TaskEntity
import com.example.core.execution.ActionExecutor
import com.example.core.execution.TaskManager
import com.example.core.model.ConfirmationRequest
import com.example.core.model.DeviceMetrics
import com.example.core.model.MemoryCategory
import com.example.core.model.MemoryItem
import com.example.core.model.RoutineItem
import com.example.core.model.TaskPlan
import com.example.core.model.VoiceState
import com.example.core.voice.SpeechRecognitionManager
import com.example.core.voice.TextToSpeechManager
import com.example.services.JarvisFloatingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

enum class AppScreen {
    CORE,
    ROUTINES,
    HISTORY,
    MEMORY,
    PERMISSIONS,
    SETTINGS,
    ONBOARDING
}

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JarvisRepository = (application as JarvisApplication).repository
    val deviceController: DeviceController = (application as JarvisApplication).deviceController
    val commsController: CommunicationController = (application as JarvisApplication).commsController

    private val speechManager = SpeechRecognitionManager(application)
    private val ttsManager = TextToSpeechManager(application)

    private val geminiProvider = GeminiAIProvider()
    private val hybridPlanner = HybridAIPlanner(geminiProvider)

    private val actionExecutor = ActionExecutor(application, deviceController, commsController, repository)
    val taskManager = TaskManager(actionExecutor, repository, viewModelScope)

    // Reactive UI States
    private val _currentScreen = MutableStateFlow(AppScreen.CORE)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _assistantName = MutableStateFlow("JARVIS")
    val assistantName: StateFlow<String> = _assistantName.asStateFlow()

    private val _latestSpokenText = MutableStateFlow("System online. At your service.")
    val latestSpokenText: StateFlow<String> = _latestSpokenText.asStateFlow()

    private val _deviceMetrics = MutableStateFlow(DeviceMetrics())
    val deviceMetrics: StateFlow<DeviceMetrics> = _deviceMetrics.asStateFlow()

    private val _isVoiceInterruptionEnabled = MutableStateFlow(true)
    val isVoiceInterruptionEnabled: StateFlow<Boolean> = _isVoiceInterruptionEnabled.asStateFlow()

    private val _floatingOverlayEnabled = MutableStateFlow(false)
    val floatingOverlayEnabled: StateFlow<Boolean> = _floatingOverlayEnabled.asStateFlow()

    private val _aiProviderMode = MutableStateFlow("Gemini 3.5 Flash")
    val aiProviderMode: StateFlow<String> = _aiProviderMode.asStateFlow()

    private val _userApiKey = MutableStateFlow("")
    val userApiKey: StateFlow<String> = _userApiKey.asStateFlow()

    val transcription: StateFlow<String> = speechManager.transcription
    val partialTranscription: StateFlow<String> = speechManager.partialTranscription
    val rmsLevel: StateFlow<Float> = speechManager.rmsLevel
    val activeTask: StateFlow<TaskPlan?> = taskManager.activeTask
    val pendingConfirmation: StateFlow<ConfirmationRequest?> = taskManager.pendingConfirmation

    // Room DB Streams
    val conversations: StateFlow<List<ConversationEntity>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<MemoryItem>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routines: StateFlow<List<RoutineItem>> = repository.allRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        speechManager.onResultListener = { recognizedQuery ->
            processUserQuery(recognizedQuery)
        }

        speechManager.onErrorListener = { errorMsg ->
            _voiceState.value = VoiceState.ERROR
            _latestSpokenText.value = errorMsg
            viewModelScope.launch {
                kotlinx.coroutines.delay(2500)
                _voiceState.value = VoiceState.IDLE
            }
        }

        ttsManager.onSpeechCompleted = {
            if (_voiceState.value == VoiceState.SPEAKING) {
                _voiceState.value = VoiceState.IDLE
            }
        }

        refreshMetrics()
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun refreshMetrics() {
        viewModelScope.launch(Dispatchers.IO) {
            _deviceMetrics.value = deviceController.getDeviceMetrics()
        }
    }

    fun startListening() {
        if (ttsManager.isSpeaking.value) {
            ttsManager.stop()
        }
        _voiceState.value = VoiceState.LISTENING
        speechManager.startListening()
    }

    fun stopListening() {
        speechManager.stopListening()
        _voiceState.value = VoiceState.IDLE
    }

    fun cancelListening() {
        speechManager.cancel()
        taskManager.cancelCurrentTask()
        ttsManager.stop()
        _voiceState.value = VoiceState.IDLE
    }

    fun injectTestCommand(commandText: String) {
        processUserQuery(commandText)
    }

    fun processUserQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        _voiceState.value = VoiceState.PROCESSING
        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            // Check for voice interruption
            val lower = trimmed.lowercase()
            if (lower in listOf("stop", "cancel", "never mind", "wait", "abort")) {
                taskManager.cancelCurrentTask()
                ttsManager.stop()
                _voiceState.value = VoiceState.IDLE
                _latestSpokenText.value = "Cancelled."
                speak("Cancelled.")
                return@launch
            }

            // Check if matches a custom routine
            val activeRoutines = repository.getActiveRoutines()
            val matchedRoutine = activeRoutines.find {
                lower.contains(it.triggerPhrase.lowercase()) || it.triggerPhrase.lowercase().contains(lower)
            }

            if (matchedRoutine != null) {
                executeRoutine(
                    RoutineItem(
                        id = matchedRoutine.id,
                        triggerPhrase = matchedRoutine.triggerPhrase,
                        name = matchedRoutine.name,
                        description = matchedRoutine.description,
                        stepsJson = matchedRoutine.stepsJson,
                        isEnabled = matchedRoutine.isEnabled,
                        iconName = matchedRoutine.iconName
                    )
                )
                return@launch
            }

            // AI planning pipeline
            val aiResponse: AIResponse = hybridPlanner.planQuery(trimmed)
            val latency = System.currentTimeMillis() - startTime

            _latestSpokenText.value = aiResponse.spokenResponse

            // Save conversation to DB
            repository.insertConversation(
                userQuery = trimmed,
                response = aiResponse.spokenResponse,
                intent = aiResponse.taskPlan?.intent ?: "CONVERSATION",
                latencyMs = latency
            )

            if (aiResponse.taskPlan != null) {
                _voiceState.value = VoiceState.EXECUTING
                speak(aiResponse.spokenResponse)

                taskManager.startTask(
                    taskPlan = aiResponse.taskPlan,
                    onStepComplete = { stepMsg ->
                        refreshMetrics()
                    },
                    onTaskFinished = { success, summary ->
                        _voiceState.value = if (success) VoiceState.IDLE else VoiceState.ERROR
                        if (!success) {
                            speak("I encountered an issue executing that command.")
                        }
                        refreshMetrics()
                    }
                )
            } else {
                _voiceState.value = VoiceState.SPEAKING
                speak(aiResponse.spokenResponse)
            }
        }
    }

    fun executeRoutine(routine: RoutineItem) {
        val steps = routine.stepsJson.split("|").filter { it.isNotBlank() }
        val actionSteps = steps.map { stepTypeStr ->
            val actionType = try {
                com.example.core.model.ActionType.valueOf(stepTypeStr)
            } catch (e: Exception) {
                com.example.core.model.ActionType.DEVICE_INFO
            }
            com.example.core.model.ActionStep(
                type = actionType,
                title = stepTypeStr.replace("_", " "),
                description = "Routine: ${routine.name}",
                params = when (actionType) {
                    com.example.core.model.ActionType.CONTROL_VOLUME -> mapOf("percent" to "50")
                    com.example.core.model.ActionType.SET_TIMER -> mapOf("seconds" to "1500")
                    com.example.core.model.ActionType.CREATE_REMINDER -> mapOf("content" to "Focus Routine Active")
                    com.example.core.model.ActionType.OPEN_APP -> mapOf("package" to "com.google.android.youtube")
                    else -> emptyMap()
                }
            )
        }

        val plan = TaskPlan(
            userQuery = routine.triggerPhrase,
            intent = "CUSTOM_ROUTINE_${routine.name.uppercase()}",
            steps = actionSteps
        )

        _voiceState.value = VoiceState.EXECUTING
        val intro = "Initiating ${routine.name} protocol."
        _latestSpokenText.value = intro
        speak(intro)

        taskManager.startTask(
            taskPlan = plan,
            onTaskFinished = { success, summary ->
                _voiceState.value = VoiceState.IDLE
                val finishMsg = "${routine.name} routine completed."
                speak(finishMsg)
                refreshMetrics()
            }
        )
    }

    fun speak(text: String) {
        _voiceState.value = VoiceState.SPEAKING
        ttsManager.speak(text)
    }

    fun stopSpeaking() {
        ttsManager.stop()
        _voiceState.value = VoiceState.IDLE
    }

    fun confirmAction() {
        taskManager.confirmPendingAction()
    }

    fun cancelAction() {
        taskManager.cancelPendingAction()
    }

    // Memory operations
    fun addMemory(category: MemoryCategory, key: String, value: String) {
        viewModelScope.launch {
            repository.insertMemory(category, key, value)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearMemories()
        }
    }

    // Routine operations
    fun addRoutine(triggerPhrase: String, name: String, description: String, stepsJson: String) {
        viewModelScope.launch {
            repository.insertRoutine(
                RoutineItem(
                    triggerPhrase = triggerPhrase,
                    name = name,
                    description = description,
                    stepsJson = stepsJson,
                    isEnabled = true
                )
            )
        }
    }

    fun toggleRoutine(routine: RoutineItem) {
        viewModelScope.launch {
            repository.updateRoutine(routine.copy(isEnabled = !routine.isEnabled))
        }
    }

    fun deleteRoutine(id: Long) {
        viewModelScope.launch {
            repository.deleteRoutine(id)
        }
    }

    // History operations
    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Settings
    fun setAssistantName(name: String) {
        _assistantName.value = name
    }

    fun setSpeechSettings(rate: Float, pitch: Float) {
        ttsManager.setSpeechParameters(rate, pitch)
    }

    fun setAiConfig(apiKey: String, model: String) {
        _userApiKey.value = apiKey
        _aiProviderMode.value = model
        geminiProvider.updateConfig(apiKey, model)
    }

    fun toggleFloatingOverlay(enable: Boolean) {
        _floatingOverlayEnabled.value = enable
        val context = getApplication<Application>()
        if (enable) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)) {
                val intent = Intent(context, JarvisFloatingService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        } else {
            val intent = Intent(context, JarvisFloatingService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCleared() {
        speechManager.destroy()
        ttsManager.shutdown()
        super.onCleared()
    }
}
