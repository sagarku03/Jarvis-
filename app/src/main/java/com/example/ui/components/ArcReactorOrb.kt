package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.model.VoiceState
import com.example.ui.theme.JarvisAccentAmber
import com.example.ui.theme.JarvisAccentGreen
import com.example.ui.theme.JarvisAccentPurple
import com.example.ui.theme.JarvisAccentRed
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisSky
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorOrb(
    voiceState: VoiceState,
    rmsLevel: Float = 0f,
    size: Dp = 220.dp,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorRotation")

    // Rotation angles
    val rotationFast by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fastRotate"
    )

    val rotationSlow by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "slowRotate"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Dynamic theme color based on VoiceState
    val (primaryColor, glowColor, secondaryColor) = when (voiceState) {
        VoiceState.IDLE -> Triple(JarvisCyan, Color(0x55D0BCFF), JarvisSky)
        VoiceState.LISTENING -> Triple(JarvisAccentGreen, Color(0x55B4E3A8), Color(0xFFC8F2BC))
        VoiceState.PROCESSING -> Triple(JarvisAccentPurple, Color(0x554F378B), Color(0xFFE8DEF8))
        VoiceState.EXECUTING -> Triple(JarvisAccentAmber, Color(0x55FFD8E4), Color(0xFFFFB4AB))
        VoiceState.SPEAKING -> Triple(JarvisSky, Color(0x55E8DEF8), JarvisCyan)
        VoiceState.ERROR -> Triple(JarvisAccentRed, Color(0x55F2B8B5), Color(0xFFFFDAD6))
    }

    val dynamicPulse = if (voiceState == VoiceState.LISTENING) {
        pulseScale * (1f + (rmsLevel * 0.25f))
    } else if (voiceState == VoiceState.SPEAKING) {
        pulseScale * 1.05f
    } else {
        pulseScale
    }

    Box(
        modifier = Modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = (this.size.minDimension / 2f) * 0.85f * dynamicPulse

            // 1. Ambient Glow Halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = center,
                    radius = radius * 1.35f
                ),
                center = center,
                radius = radius * 1.35f
            )

            // 2. Outer Segmented Ring (Rotating Slow)
            rotate(rotationSlow, pivot = center) {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.35f),
                    center = center,
                    radius = radius,
                    style = Stroke(
                        width = 2.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(40f, 15f, 10f, 15f), 0f)
                    )
                )
            }

            // 3. Middle High-Tech Arc Ring (Rotating Fast)
            rotate(rotationFast, pivot = center) {
                val midRadius = radius * 0.78f
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.8f),
                    center = center,
                    radius = midRadius,
                    style = Stroke(
                        width = 4f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(60f, 30f), 0f)
                    )
                )

                // Arc Nodes / Satellites
                for (i in 0 until 6) {
                    val angle = (i * (360f / 6f)) * (Math.PI / 180f)
                    val nodeX = center.x + (midRadius * cos(angle)).toFloat()
                    val nodeY = center.y + (midRadius * sin(angle)).toFloat()
                    drawCircle(
                        color = primaryColor,
                        center = Offset(nodeX, nodeY),
                        radius = 4f
                    )
                }
            }

            // 4. Inner Ring with Crosshairs
            val innerRadius = radius * 0.52f
            drawCircle(
                color = primaryColor.copy(alpha = 0.5f),
                center = center,
                radius = innerRadius,
                style = Stroke(width = 2f)
            )

            // Crosshair Ticks
            val tickLen = 8f
            drawLine(
                color = primaryColor,
                start = Offset(center.x, center.y - innerRadius - tickLen),
                end = Offset(center.x, center.y - innerRadius + tickLen),
                strokeWidth = 2.5f
            )
            drawLine(
                color = primaryColor,
                start = Offset(center.x, center.y + innerRadius - tickLen),
                end = Offset(center.x, center.y + innerRadius + tickLen),
                strokeWidth = 2.5f
            )
            drawLine(
                color = primaryColor,
                start = Offset(center.x - innerRadius - tickLen, center.y),
                end = Offset(center.x - innerRadius + tickLen, center.y),
                strokeWidth = 2.5f
            )
            drawLine(
                color = primaryColor,
                start = Offset(center.x + innerRadius - tickLen, center.y),
                end = Offset(center.x + innerRadius + tickLen, center.y),
                strokeWidth = 2.5f
            )

            // 5. Glowing Central Core Reactor
            val coreRadius = radius * 0.28f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, primaryColor, primaryColor.copy(alpha = 0.15f)),
                    center = center,
                    radius = coreRadius
                ),
                center = center,
                radius = coreRadius
            )

            drawCircle(
                color = Color.White,
                center = center,
                radius = coreRadius * 0.45f
            )
        }
    }
}
