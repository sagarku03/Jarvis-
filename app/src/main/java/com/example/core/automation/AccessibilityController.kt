package com.example.core.automation

import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import com.example.services.JarvisAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

object AccessibilityController {

    private var serviceRef: WeakReference<JarvisAccessibilityService>? = null

    private val _isServiceActive = MutableStateFlow(false)
    val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

    private val _currentAppPackage = MutableStateFlow("")
    val currentAppPackage: StateFlow<String> = _currentAppPackage.asStateFlow()

    fun registerService(service: JarvisAccessibilityService) {
        serviceRef = WeakReference(service)
        _isServiceActive.value = true
    }

    fun unregisterService(service: JarvisAccessibilityService) {
        if (serviceRef?.get() == service) {
            serviceRef = null
            _isServiceActive.value = false
        }
    }

    fun updateCurrentPackage(packageName: String) {
        _currentAppPackage.value = packageName
    }

    fun getService(): JarvisAccessibilityService? {
        return serviceRef?.get()
    }

    fun isAccessibilitySettingsEnabled(context: Context): Boolean {
        var accessibilityEnabled = 0
        val service = context.packageName + "/" + JarvisAccessibilityService::class.java.canonicalName
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                context.applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Settings.SettingNotFoundException) {
            return false
        }
        val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(
                context.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext()) {
                    val accessibilityService = mStringColonSplitter.next()
                    if (accessibilityService.equals(service, ignoreCase = true)) {
                        return true
                    }
                }
            }
        }
        return isServiceActive.value
    }
}
