package com.example.core.automation

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.AlarmClock
import android.provider.Settings
import com.example.core.model.DeviceMetrics
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DeviceController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var isTorchOn: Boolean = false

    fun getDeviceMetrics(): DeviceMetrics {
        // Battery
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        // Volume
        val maxVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        val curVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 10
        val volPct = (curVol.toFloat() / maxVol.toFloat() * 100).toInt()

        // Storage
        val stat = StatFs(Environment.getDataDirectory().path)
        val bytesAvailable = stat.availableBlocksLong * stat.blockSizeLong
        val storageGb = bytesAvailable / (1024f * 1024f * 1024f)

        // Memory
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val totalRamGb = memInfo.totalMem / (1024f * 1024f * 1024f)
        val freeRamGb = memInfo.availMem / (1024f * 1024f * 1024f)

        // Time
        val timeStr = SimpleDateFormat("h:mm a, EEEE, MMM d", Locale.getDefault()).format(Date())

        return DeviceMetrics(
            batteryLevel = batteryPct,
            isCharging = isCharging,
            volumePercent = volPct,
            storageAvailableGb = String.format(Locale.US, "%.1f", storageGb).toFloat(),
            totalRamGb = String.format(Locale.US, "%.1f", totalRamGb).toFloat(),
            freeRamGb = String.format(Locale.US, "%.1f", freeRamGb).toFloat(),
            isFlashlightOn = isTorchOn,
            isAccessibilityEnabled = AccessibilityController.isAccessibilitySettingsEnabled(context),
            isOverlayPermissionEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true,
            isMicPermissionGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED,
            timestampFormatted = timeStr
        )
    }

    fun setVolume(percent: Int): Boolean {
        if (audioManager == null) return false
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetVol = ((percent.coerceIn(0, 100) / 100f) * maxVol).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, AudioManager.FLAG_SHOW_UI)
        return true
    }

    fun toggleFlashlight(enable: Boolean): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && cameraManager != null) {
                val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return false
                cameraManager.setTorchMode(cameraId, enable)
                isTorchOn = enable
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun openSettings(type: String): Boolean {
        return try {
            val intent = when (type.lowercase()) {
                "wifi" -> Intent(Settings.ACTION_WIFI_SETTINGS)
                "bluetooth" -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                "display" -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
                "sound", "volume" -> Intent(Settings.ACTION_SOUND_SETTINGS)
                "battery" -> Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
                "accessibility" -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                "overlay" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                } else {
                    Intent(Settings.ACTION_SETTINGS)
                }
                "notification_access", "notifications" -> Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                "apps" -> Intent(Settings.ACTION_APPLICATION_SETTINGS)
                else -> Intent(Settings.ACTION_SETTINGS)
            }.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setAlarm(hour: Int, minutes: Int, message: String = "JARVIS Alarm"): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setTimer(seconds: Int, message: String = "JARVIS Timer"): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
