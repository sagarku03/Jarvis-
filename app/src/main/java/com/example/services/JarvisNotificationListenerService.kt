package com.example.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.core.model.CapturedNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class JarvisNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        isServiceConnected = true
        Log.d("JarvisNotification", "Notification Listener Connected")
        refreshActiveNotifications()
    }

    override fun onListenerDisconnected() {
        isServiceConnected = false
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val packageName = sbn.packageName ?: ""
        val appName = try {
            val pm = applicationContext.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName.substringAfterLast('.')
        }

        if (title.isNotBlank() || text.isNotBlank()) {
            val notification = CapturedNotification(
                id = sbn.key ?: "${sbn.id}_${sbn.postTime}",
                packageName = packageName,
                appName = appName,
                title = title,
                text = text,
                postTime = sbn.postTime,
                isRead = false
            )
            val currentList = _notificationsFlow.value.toMutableList()
            // Remove previous with same id or update
            currentList.removeAll { it.id == notification.id }
            currentList.add(0, notification)
            _notificationsFlow.value = currentList.take(50)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val id = sbn.key ?: "${sbn.id}_${sbn.postTime}"
        val currentList = _notificationsFlow.value.toMutableList()
        currentList.removeAll { it.id == id }
        _notificationsFlow.value = currentList
    }

    fun refreshActiveNotifications() {
        try {
            val activeSbns = activeNotifications ?: return
            val list = mutableListOf<CapturedNotification>()
            for (sbn in activeSbns) {
                val extras = sbn.notification.extras
                val title = extras.getString("android.title") ?: ""
                val text = extras.getCharSequence("android.text")?.toString() ?: ""
                val packageName = sbn.packageName ?: ""
                val appName = try {
                    val pm = applicationContext.packageManager
                    val appInfo = pm.getApplicationInfo(packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    packageName.substringAfterLast('.')
                }
                if (title.isNotBlank() || text.isNotBlank()) {
                    list.add(
                        CapturedNotification(
                            id = sbn.key ?: "${sbn.id}_${sbn.postTime}",
                            packageName = packageName,
                            appName = appName,
                            title = title,
                            text = text,
                            postTime = sbn.postTime,
                            isRead = false
                        )
                    )
                }
            }
            _notificationsFlow.value = list
        } catch (e: Exception) {
            Log.e("JarvisNotification", "Error refreshing notifications", e)
        }
    }

    companion object {
        var isServiceConnected: Boolean = false
        private val _notificationsFlow = MutableStateFlow<List<CapturedNotification>>(emptyList())
        val notificationsFlow: StateFlow<List<CapturedNotification>> = _notificationsFlow.asStateFlow()

        fun getLatestSummary(): String {
            val list = _notificationsFlow.value
            if (list.isEmpty()) {
                return "You have no unread notifications at the moment."
            }
            val appGroups = list.groupBy { it.appName }
            val summaryParts = appGroups.map { (app, items) ->
                "${items.size} from $app"
            }
            val details = list.take(3).joinToString("; ") { "${it.appName}: ${it.title} - ${it.text}" }
            return "You have ${list.size} active notifications (${summaryParts.joinToString(", ")}). Recent: $details"
        }
    }
}
