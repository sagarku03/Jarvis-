package com.example.core.model

data class DeviceMetrics(
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false,
    val wifiEnabled: Boolean = true,
    val wifiSsid: String = "Connected",
    val bluetoothEnabled: Boolean = false,
    val ringerMode: String = "Normal",
    val volumePercent: Int = 70,
    val storageAvailableGb: Float = 64.0f,
    val totalRamGb: Float = 8.0f,
    val freeRamGb: Float = 4.2f,
    val isFlashlightOn: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val isNotificationAccessEnabled: Boolean = false,
    val isOverlayPermissionEnabled: Boolean = false,
    val isMicPermissionGranted: Boolean = false,
    val timestampFormatted: String = ""
)
