package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.theme.SophisticatedOnSecondary
import com.example.ui.theme.SophisticatedSecondary
import com.example.ui.theme.SophisticatedSurfaceDark
import com.example.ui.theme.SophisticatedTextMuted
import com.example.ui.theme.SophisticatedTextSecondary

@Composable
fun BottomNav(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SophisticatedSurfaceDark)
            .navigationBarsPadding()
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Default.Hub,
                label = "Core",
                isSelected = currentScreen == AppScreen.CORE,
                onClick = { onNavigate(AppScreen.CORE) }
            )
            NavItem(
                icon = Icons.Default.ElectricBolt,
                label = "Routines",
                isSelected = currentScreen == AppScreen.ROUTINES,
                onClick = { onNavigate(AppScreen.ROUTINES) }
            )
            NavItem(
                icon = Icons.Default.History,
                label = "History",
                isSelected = currentScreen == AppScreen.HISTORY,
                onClick = { onNavigate(AppScreen.HISTORY) }
            )
            NavItem(
                icon = Icons.Default.Memory,
                label = "Memory",
                isSelected = currentScreen == AppScreen.MEMORY,
                onClick = { onNavigate(AppScreen.MEMORY) }
            )
            NavItem(
                icon = Icons.Default.Security,
                label = "Access",
                isSelected = currentScreen == AppScreen.PERMISSIONS,
                onClick = { onNavigate(AppScreen.PERMISSIONS) }
            )
            NavItem(
                icon = Icons.Default.Settings,
                label = "Config",
                isSelected = currentScreen == AppScreen.SETTINGS,
                onClick = { onNavigate(AppScreen.SETTINGS) }
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val pillBgColor by animateColorAsState(
        targetValue = if (isSelected) SophisticatedSecondary else Color.Transparent,
        animationSpec = tween(200),
        label = "navPillBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) SophisticatedOnSecondary else SophisticatedTextSecondary,
        animationSpec = tween(200),
        label = "navContentColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(pillBgColor)
                .padding(horizontal = 14.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) SophisticatedSecondary else SophisticatedTextMuted,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
