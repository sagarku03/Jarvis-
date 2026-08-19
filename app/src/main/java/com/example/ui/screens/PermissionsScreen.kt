package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.automation.AccessibilityController
import com.example.services.JarvisNotificationListenerService
import com.example.ui.JarvisViewModel
import com.example.ui.theme.SophisticatedAccentAmber
import com.example.ui.theme.SophisticatedAccentGreen
import com.example.ui.theme.SophisticatedAccentRed
import com.example.ui.theme.SophisticatedBgDark
import com.example.ui.theme.SophisticatedCardBg
import com.example.ui.theme.SophisticatedCardBorder
import com.example.ui.theme.SophisticatedPrimary
import com.example.ui.theme.SophisticatedSecondary
import com.example.ui.theme.SophisticatedTertiary
import com.example.ui.theme.SophisticatedTertiaryContainer
import com.example.ui.theme.SophisticatedTextMuted
import com.example.ui.theme.SophisticatedTextPrimary
import com.example.ui.theme.SophisticatedTextSecondary

@Composable
fun PermissionsScreen(
    viewModel: JarvisViewModel,
    onRequestMicrophone: () -> Unit,
    onRequestContacts: () -> Unit,
    onRequestPhone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val metrics by viewModel.deviceMetrics.collectAsState()
    val scrollState = rememberScrollState()

    val isAccessibilityOk = AccessibilityController.isAccessibilitySettingsEnabled(context)
    val isNotificationOk = JarvisNotificationListenerService.isServiceConnected
    val isOverlayOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBgDark)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SECURITY & INTEGRATIONS",
                        color = SophisticatedPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Access Center",
                        color = SophisticatedTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { viewModel.refreshMetrics() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SophisticatedCardBg,
                        contentColor = SophisticatedPrimary
                    ),
                    modifier = Modifier.border(1.dp, SophisticatedCardBorder, RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Check", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // System Readiness Banner
            val allCriticalReady = metrics.isMicPermissionGranted && isAccessibilityOk
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (allCriticalReady) Color(0xFF1E2F23) else Color(0xFF352620))
                    .border(
                        1.dp,
                        if (allCriticalReady) SophisticatedAccentGreen.copy(alpha = 0.5f) else SophisticatedAccentAmber.copy(alpha = 0.5f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (allCriticalReady) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (allCriticalReady) SophisticatedAccentGreen else SophisticatedAccentAmber,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (allCriticalReady) "SYSTEM OPERATIONAL" else "CONFIG REQUIRED",
                            color = if (allCriticalReady) SophisticatedAccentGreen else SophisticatedAccentAmber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (allCriticalReady) "All essential core automation permissions are active." else "Grant microphone and accessibility for full autonomous voice control.",
                            color = SophisticatedTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Microphone Permission Card
            PermissionCard(
                icon = Icons.Default.Mic,
                title = "Voice Input (Microphone)",
                description = "Required for real-time speech recognition and voice commands.",
                isGranted = metrics.isMicPermissionGranted,
                actionLabel = "Grant Mic",
                onAction = onRequestMicrophone
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Accessibility Automation Service
            PermissionCard(
                icon = Icons.Default.Accessibility,
                title = "Accessibility Automation Engine",
                description = "Enables autonomous UI navigation (back, home, scrolling, clicking buttons, screenshot).",
                isGranted = isAccessibilityOk,
                actionLabel = "Enable Service",
                onAction = {
                    viewModel.deviceController.openSettings("accessibility")
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Notification Access Service
            PermissionCard(
                icon = Icons.Default.NotificationsActive,
                title = "Notification Intelligence",
                description = "Allows JARVIS to read incoming message alerts and summarize unread updates.",
                isGranted = isNotificationOk,
                actionLabel = "Enable Listener",
                onAction = {
                    viewModel.deviceController.openSettings("notification_access")
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Floating Overlay Window
            PermissionCard(
                icon = Icons.Default.Layers,
                title = "Floating Arc-Reactor Bubble",
                description = "Enables the persistent floating assistant on top of other Android apps.",
                isGranted = isOverlayOk,
                actionLabel = "Allow Overlay",
                onAction = {
                    viewModel.deviceController.openSettings("overlay")
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Contacts Resolution
            PermissionCard(
                icon = Icons.Default.Contacts,
                title = "Contacts Integration",
                description = "Used to lookup contacts for calling and text message drafting.",
                isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED,
                actionLabel = "Grant Contacts",
                onAction = onRequestContacts
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 6. Direct Phone Calling
            PermissionCard(
                icon = Icons.Default.Phone,
                title = "Phone Calling",
                description = "Required for instant phone dialing commands.",
                isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CALL_PHONE
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED,
                actionLabel = "Grant Phone",
                onAction = onRequestPhone
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SophisticatedCardBg)
            .border(1.dp, SophisticatedCardBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isGranted) Color(0xFF1E2F23) else SophisticatedTertiary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) SophisticatedAccentGreen else SophisticatedPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        color = SophisticatedTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        color = SophisticatedTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGranted) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E2F23))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        color = SophisticatedAccentGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SophisticatedPrimary,
                        contentColor = SophisticatedTertiary
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(actionLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
