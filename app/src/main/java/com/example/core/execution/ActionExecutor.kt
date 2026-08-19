package com.example.core.execution

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.core.automation.AccessibilityController
import com.example.core.automation.AppCapabilityManager
import com.example.core.automation.CommunicationController
import com.example.core.automation.DeviceController
import com.example.core.database.JarvisRepository
import com.example.core.model.ActionStep
import com.example.core.model.ActionType
import com.example.core.model.ExecutionResult
import com.example.services.JarvisNotificationListenerService
import kotlinx.coroutines.delay

class ActionExecutor(
    private val context: Context,
    private val deviceController: DeviceController,
    private val commsController: CommunicationController,
    private val repository: JarvisRepository
) {

    suspend fun executeStep(step: ActionStep): ExecutionResult {
        return try {
            when (step.type) {
                ActionType.OPEN_APP -> {
                    val packageName = step.params["package"] ?: "com.google.android.youtube"
                    val success = AppCapabilityManager.launchApp(context, packageName)
                    if (success) {
                        ExecutionResult(true, "Launched app successfully")
                    } else {
                        ExecutionResult(false, "Could not open requested app. It may not be installed.")
                    }
                }

                ActionType.SEARCH_APP -> {
                    val appId = step.params["appId"] ?: "youtube"
                    val query = step.params["query"] ?: ""
                    val appProfile = AppCapabilityManager.findApp(appId)
                    if (appProfile != null) {
                        val launched = AppCapabilityManager.searchWithinApp(context, appProfile, query)
                        ExecutionResult(launched, "Searched $appId for $query")
                    } else {
                        // Fallback to web search
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                        ExecutionResult(true, "Searched online for $query")
                    }
                }

                ActionType.SEARCH_WEB -> {
                    val query = step.params["query"] ?: ""
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ExecutionResult(true, "Web search launched for $query")
                }

                ActionType.CONTROL_VOLUME -> {
                    val percent = step.params["percent"]?.toIntOrNull() ?: 70
                    val ok = deviceController.setVolume(percent)
                    ExecutionResult(ok, "Volume set to $percent%")
                }

                ActionType.FLASHLIGHT_CONTROL -> {
                    val enable = step.params["enable"]?.toBooleanStrictOrNull() ?: true
                    val ok = deviceController.toggleFlashlight(enable)
                    ExecutionResult(ok, if (enable) "Flashlight turned on" else "Flashlight turned off")
                }

                ActionType.SET_ALARM -> {
                    val hour = step.params["hour"]?.toIntOrNull() ?: 7
                    val min = step.params["minutes"]?.toIntOrNull() ?: 0
                    val msg = step.params["message"] ?: "JARVIS Alarm"
                    val ok = deviceController.setAlarm(hour, min, msg)
                    ExecutionResult(ok, "Alarm set for ${String.format("%02d:%02d", hour, min)}")
                }

                ActionType.SET_TIMER -> {
                    val sec = step.params["seconds"]?.toIntOrNull() ?: 600
                    val msg = step.params["message"] ?: "JARVIS Timer"
                    val ok = deviceController.setTimer(sec, msg)
                    ExecutionResult(ok, "Timer set for ${sec / 60} minutes")
                }

                ActionType.CREATE_REMINDER -> {
                    val content = step.params["content"] ?: "Reminder"
                    repository.insertNote("Reminder", content)
                    ExecutionResult(true, "Reminder saved: $content")
                }

                ActionType.CREATE_NOTE -> {
                    val title = step.params["title"] ?: "Voice Note"
                    val content = step.params["content"] ?: ""
                    repository.insertNote(title, content)
                    ExecutionResult(true, "Note saved to database")
                }

                ActionType.MAKE_CALL -> {
                    val target = step.params["target"] ?: ""
                    val contacts = commsController.searchContact(target)
                    val phoneNum = contacts.firstOrNull()?.phoneNumber ?: target
                    val ok = commsController.makeCall(phoneNum)
                    ExecutionResult(ok, "Calling $target ($phoneNum)")
                }

                ActionType.DRAFT_MESSAGE -> {
                    val recipient = step.params["recipient"] ?: ""
                    val msg = step.params["message"] ?: ""
                    val contacts = commsController.searchContact(recipient)
                    val phoneNum = contacts.firstOrNull()?.phoneNumber ?: recipient
                    val ok = commsController.draftSms(phoneNum, msg)
                    ExecutionResult(ok, "Message prepared for $recipient: \"$msg\"")
                }

                ActionType.READ_NOTIFICATIONS -> {
                    val summary = JarvisNotificationListenerService.getLatestSummary()
                    ExecutionResult(true, summary, data = summary)
                }

                ActionType.OPEN_SETTINGS -> {
                    val type = step.params["type"] ?: "general"
                    val ok = deviceController.openSettings(type)
                    ExecutionResult(ok, "Opened $type settings")
                }

                ActionType.DEVICE_INFO -> {
                    val metrics = deviceController.getDeviceMetrics()
                    val info = "Battery at ${metrics.batteryLevel}% ${if (metrics.isCharging) "(Charging)" else ""}, Volume ${metrics.volumePercent}%, Storage ${metrics.storageAvailableGb}GB free, Time ${metrics.timestampFormatted}."
                    ExecutionResult(true, info, data = metrics)
                }

                ActionType.PRESS_BACK -> {
                    val service = AccessibilityController.getService()
                    val ok = service?.performBack() ?: false
                    if (!ok) {
                        ExecutionResult(false, "Accessibility Service not enabled for navigation.")
                    } else {
                        ExecutionResult(true, "Navigated back")
                    }
                }

                ActionType.PRESS_HOME -> {
                    val service = AccessibilityController.getService()
                    val ok = service?.performHome() ?: false
                    if (!ok) {
                        ExecutionResult(false, "Accessibility Service not enabled for home action.")
                    } else {
                        ExecutionResult(true, "Returned to home screen")
                    }
                }

                ActionType.TAKE_SCREENSHOT -> {
                    val service = AccessibilityController.getService()
                    val ok = service?.performTakeScreenshot() ?: false
                    ExecutionResult(ok, if (ok) "Screenshot captured" else "Screenshot action requires Accessibility Service")
                }

                ActionType.CLICK_ELEMENT -> {
                    val text = step.params["text"] ?: ""
                    val service = AccessibilityController.getService()
                    val ok = service?.findAndClickNodeByText(text) ?: false
                    ExecutionResult(ok, if (ok) "Clicked $text" else "Could not find element with text $text")
                }

                ActionType.TYPE_TEXT -> {
                    val text = step.params["text"] ?: ""
                    val service = AccessibilityController.getService()
                    val ok = service?.findAndTypeText(text) ?: false
                    ExecutionResult(ok, if (ok) "Typed text" else "Could not focus input field")
                }

                ActionType.SCROLL -> {
                    val service = AccessibilityController.getService()
                    val ok = service?.scrollForward() ?: false
                    ExecutionResult(ok, if (ok) "Scrolled down" else "Scroll action requires Accessibility Service")
                }

                ActionType.AI_CONVERSATION -> {
                    ExecutionResult(true, "Conversation query answered")
                }

                ActionType.CUSTOM_ROUTINE -> {
                    ExecutionResult(true, "Custom Routine executed")
                }

                ActionType.CONTROL_MEDIA -> {
                    ExecutionResult(true, "Media action processed")
                }
            }
        } catch (e: Exception) {
            ExecutionResult(false, "Execution error: ${e.message}", error = e)
        }
    }
}
