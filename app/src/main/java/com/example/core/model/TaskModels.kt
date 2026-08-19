package com.example.core.model

enum class TaskState {
    QUEUED,
    PLANNING,
    WAITING_PERMISSION,
    WAITING_CONFIRMATION,
    EXECUTING,
    VERIFYING,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class ActionType {
    OPEN_APP,
    SEARCH_APP,
    SEARCH_WEB,
    CLICK_ELEMENT,
    TYPE_TEXT,
    SCROLL,
    PRESS_BACK,
    PRESS_HOME,
    TAKE_SCREENSHOT,
    SET_ALARM,
    SET_TIMER,
    CREATE_REMINDER,
    CREATE_NOTE,
    MAKE_CALL,
    DRAFT_MESSAGE,
    READ_NOTIFICATIONS,
    CONTROL_MEDIA,
    CONTROL_VOLUME,
    FLASHLIGHT_CONTROL,
    OPEN_SETTINGS,
    DEVICE_INFO,
    AI_CONVERSATION,
    CUSTOM_ROUTINE
}

data class ActionStep(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: ActionType,
    val title: String,
    val description: String,
    val params: Map<String, String> = emptyMap(),
    val requiresConfirmation: Boolean = false,
    val requiredPermission: String? = null,
    var isCompleted: Boolean = false,
    var statusMessage: String = "Pending",
    var errorMessage: String? = null
)

data class TaskPlan(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userQuery: String,
    val intent: String,
    val steps: List<ActionStep>,
    var currentStepIndex: Int = 0,
    var state: TaskState = TaskState.QUEUED,
    val createdAt: Long = System.currentTimeMillis(),
    var completedAt: Long? = null,
    var resultSummary: String = ""
) {
    val currentStep: ActionStep?
        get() = steps.getOrNull(currentStepIndex)

    val progressPercent: Float
        get() {
            if (steps.isEmpty()) return 1f
            val doneCount = steps.count { it.isCompleted }
            return doneCount.toFloat() / steps.size.toFloat()
        }
}

data class ExecutionResult(
    val success: Boolean,
    val message: String,
    val data: Any? = null,
    val error: Throwable? = null
)

data class ConfirmationRequest(
    val id: String = java.util.UUID.randomUUID().toString(),
    val actionType: ActionType,
    val title: String,
    val message: String,
    val target: String = "",
    val details: String = "",
    val onConfirm: () -> Unit,
    val onCancel: () -> Unit
)
