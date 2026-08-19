package com.example.core.execution

import com.example.core.database.JarvisRepository
import com.example.core.database.TaskEntity
import com.example.core.model.ConfirmationRequest
import com.example.core.model.ExecutionResult
import com.example.core.model.TaskPlan
import com.example.core.model.TaskState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskManager(
    private val actionExecutor: ActionExecutor,
    private val repository: JarvisRepository,
    private val coroutineScope: CoroutineScope
) {

    private val _activeTask = MutableStateFlow<TaskPlan?>(null)
    val activeTask: StateFlow<TaskPlan?> = _activeTask.asStateFlow()

    private val _pendingConfirmation = MutableStateFlow<ConfirmationRequest?>(null)
    val pendingConfirmation: StateFlow<ConfirmationRequest?> = _pendingConfirmation.asStateFlow()

    private var executionJob: Job? = null

    fun startTask(
        taskPlan: TaskPlan,
        onStepComplete: ((String) -> Unit)? = null,
        onTaskFinished: ((Boolean, String) -> Unit)? = null
    ) {
        cancelCurrentTask()

        _activeTask.value = taskPlan
        taskPlan.state = TaskState.EXECUTING

        executionJob = coroutineScope.launch(Dispatchers.Main) {
            var overallSuccess = true
            val resultsLog = StringBuilder()

            for ((index, step) in taskPlan.steps.withIndex()) {
                if (taskPlan.state == TaskState.CANCELLED) break

                taskPlan.currentStepIndex = index
                _activeTask.value = taskPlan.copy(currentStepIndex = index)

                // Check if user confirmation is needed (e.g. making a call, sending SMS)
                if (step.requiresConfirmation) {
                    taskPlan.state = TaskState.WAITING_CONFIRMATION
                    _activeTask.value = taskPlan.copy(state = TaskState.WAITING_CONFIRMATION)

                    val confirmed = suspendUntilConfirmed(step.title, step.description, step.type)
                    if (!confirmed) {
                        step.statusMessage = "Cancelled by user"
                        step.errorMessage = "User declined confirmation"
                        taskPlan.state = TaskState.CANCELLED
                        taskPlan.resultSummary = "Task cancelled by user."
                        _activeTask.value = taskPlan.copy(state = TaskState.CANCELLED)
                        onTaskFinished?.invoke(false, "Cancelled by user.")
                        saveTaskToDb(taskPlan)
                        return@launch
                    }
                }

                taskPlan.state = TaskState.EXECUTING
                step.statusMessage = "Executing..."
                _activeTask.value = taskPlan.copy(state = TaskState.EXECUTING)

                val result = actionExecutor.executeStep(step)
                if (result.success) {
                    step.isCompleted = true
                    step.statusMessage = result.message
                    resultsLog.append("${step.title}: Success. ")
                    onStepComplete?.invoke(result.message)
                } else {
                    overallSuccess = false
                    step.isCompleted = false
                    step.statusMessage = "Failed"
                    step.errorMessage = result.message
                    resultsLog.append("${step.title}: Failed (${result.message}). ")
                    taskPlan.state = TaskState.FAILED
                    taskPlan.resultSummary = result.message
                    _activeTask.value = taskPlan.copy(state = TaskState.FAILED)
                    onTaskFinished?.invoke(false, result.message)
                    saveTaskToDb(taskPlan)
                    return@launch
                }
            }

            if (taskPlan.state != TaskState.CANCELLED) {
                taskPlan.state = if (overallSuccess) TaskState.COMPLETED else TaskState.FAILED
                taskPlan.completedAt = System.currentTimeMillis()
                taskPlan.resultSummary = if (overallSuccess) "Task completed successfully." else "Task execution had issues."
                _activeTask.value = taskPlan.copy(state = taskPlan.state)
                onTaskFinished?.invoke(overallSuccess, taskPlan.resultSummary)
            }

            saveTaskToDb(taskPlan)
        }
    }

    private suspend fun suspendUntilConfirmed(title: String, desc: String, actionType: com.example.core.model.ActionType): Boolean {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            _pendingConfirmation.value = ConfirmationRequest(
                actionType = actionType,
                title = title,
                message = desc,
                onConfirm = {
                    _pendingConfirmation.value = null
                    if (continuation.isActive) continuation.resumeWith(Result.success(true))
                },
                onCancel = {
                    _pendingConfirmation.value = null
                    if (continuation.isActive) continuation.resumeWith(Result.success(false))
                }
            )
        }
    }

    fun confirmPendingAction() {
        _pendingConfirmation.value?.onConfirm?.invoke()
    }

    fun cancelPendingAction() {
        _pendingConfirmation.value?.onCancel?.invoke()
    }

    fun cancelCurrentTask() {
        executionJob?.cancel()
        _pendingConfirmation.value = null
        _activeTask.value?.let { task ->
            task.state = TaskState.CANCELLED
            task.resultSummary = "Task interrupted."
            _activeTask.value = task.copy(state = TaskState.CANCELLED)
            coroutineScope.launch(Dispatchers.IO) {
                saveTaskToDb(task)
            }
        }
    }

    private suspend fun saveTaskToDb(task: TaskPlan) {
        val entity = TaskEntity(
            id = task.id,
            userQuery = task.userQuery,
            intent = task.intent,
            stepsSummary = task.steps.joinToString(" -> ") { "${it.title}: ${it.statusMessage}" },
            status = task.state.name,
            startTime = task.createdAt,
            endTime = task.completedAt ?: System.currentTimeMillis(),
            resultSummary = task.resultSummary
        )
        repository.insertTask(entity)
    }
}
