package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.model.VoiceState
import com.example.ui.theme.SophisticatedAccentAmber
import com.example.ui.theme.SophisticatedAccentGreen
import com.example.ui.theme.SophisticatedAccentPurple
import com.example.ui.theme.SophisticatedAccentRed
import com.example.ui.theme.SophisticatedPrimary
import com.example.ui.theme.SophisticatedSecondary

@Composable
fun AudioWaveformVisualizer(
    rmsLevel: Float,
    voiceState: VoiceState,
    modifier: Modifier = Modifier,
    barCount: Int = 18,
    maxHeight: Dp = 44.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformBars")

    val baseColor = when (voiceState) {
        VoiceState.LISTENING -> SophisticatedAccentGreen
        VoiceState.PROCESSING -> SophisticatedAccentPurple
        VoiceState.EXECUTING -> SophisticatedAccentAmber
        VoiceState.SPEAKING -> SophisticatedSecondary
        VoiceState.ERROR -> SophisticatedAccentRed
        else -> SophisticatedPrimary
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val animFraction by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 350 + (i * 45) % 400,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )

            val heightMultiplier = when (voiceState) {
                VoiceState.LISTENING -> (rmsLevel * 0.8f + animFraction * 0.4f).coerceIn(0.15f, 1f)
                VoiceState.SPEAKING -> (0.4f + animFraction * 0.6f).coerceIn(0.2f, 1f)
                VoiceState.PROCESSING -> (0.25f + animFraction * 0.5f).coerceIn(0.2f, 0.75f)
                VoiceState.EXECUTING -> (0.35f + animFraction * 0.5f).coerceIn(0.2f, 0.85f)
                else -> 0.12f
            }

            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(maxHeight * heightMultiplier)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(baseColor, baseColor.copy(alpha = 0.35f))
                        )
                    )
            )
        }
    }
}
