package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.DeviceMetrics
import com.example.ui.theme.SophisticatedAccentGreen
import com.example.ui.theme.SophisticatedAccentAmber
import com.example.ui.theme.SophisticatedCardBg
import com.example.ui.theme.SophisticatedCardBorder
import com.example.ui.theme.SophisticatedPrimary
import com.example.ui.theme.SophisticatedTertiaryContainer
import com.example.ui.theme.SophisticatedTextPrimary
import com.example.ui.theme.SophisticatedTextSecondary

@Composable
fun StatusHud(
    assistantName: String,
    deviceMetrics: DeviceMetrics,
    aiModel: String,
    onStatusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SophisticatedCardBg)
            .border(1.dp, SophisticatedCardBorder, RoundedCornerShape(20.dp))
            .clickable { onStatusClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Assistant Identity & Live Beacon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(SophisticatedAccentGreen)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = assistantName.uppercase(),
                        color = SophisticatedPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = aiModel,
                        color = SophisticatedTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Right: Telemetry Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Battery metric
                HudMetricChip(
                    icon = if (deviceMetrics.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                    text = "${deviceMetrics.batteryLevel}%",
                    color = if (deviceMetrics.batteryLevel > 20) SophisticatedPrimary else SophisticatedAccentAmber
                )

                // Volume metric
                HudMetricChip(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    text = "${deviceMetrics.volumePercent}%",
                    color = SophisticatedTextPrimary
                )

                // Accessibility & Permissions Shield
                val isAccessReady = deviceMetrics.isAccessibilityEnabled && deviceMetrics.isMicPermissionGranted
                HudMetricChip(
                    icon = Icons.Default.Security,
                    text = if (isAccessReady) "READY" else "CONFIG",
                    color = if (isAccessReady) SophisticatedAccentGreen else SophisticatedAccentAmber
                )
            }
        }
    }
}

@Composable
private fun HudMetricChip(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SophisticatedTertiaryContainer)
            .padding(horizontal = 7.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
