package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.VoiceState
import com.example.ui.AppScreen
import com.example.ui.JarvisViewModel
import com.example.ui.components.ArcReactorOrb
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.StatusHud
import com.example.ui.components.TaskProgressCard
import com.example.ui.theme.SophisticatedAccentAmber
import com.example.ui.theme.SophisticatedAccentGreen
import com.example.ui.theme.SophisticatedAccentPurple
import com.example.ui.theme.SophisticatedAccentRed
import com.example.ui.theme.SophisticatedBgDark
import com.example.ui.theme.SophisticatedCardBg
import com.example.ui.theme.SophisticatedCardBorder
import com.example.ui.theme.SophisticatedPrimary
import com.example.ui.theme.SophisticatedSecondary
import com.example.ui.theme.SophisticatedSurfaceDark
import com.example.ui.theme.SophisticatedTertiaryContainer
import com.example.ui.theme.SophisticatedTextMuted
import com.example.ui.theme.SophisticatedTextPrimary
import com.example.ui.theme.SophisticatedTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: JarvisViewModel,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val voiceState by viewModel.voiceState.collectAsState()
    val assistantName by viewModel.assistantName.collectAsState()
    val spokenText by viewModel.latestSpokenText.collectAsState()
    val partialText by viewModel.partialTranscription.collectAsState()
    val rmsLevel by viewModel.rmsLevel.collectAsState()
    val activeTask by viewModel.activeTask.collectAsState()
    val pendingConfirmation by viewModel.pendingConfirmation.collectAsState()
    val deviceMetrics by viewModel.deviceMetrics.collectAsState()
    val aiModel by viewModel.aiProviderMode.collectAsState()

    var manualInputText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Confirmation Alert
    pendingConfirmation?.let { req ->
        ConfirmationDialog(
            request = req,
            onConfirm = { viewModel.confirmAction() },
            onDismiss = { viewModel.cancelAction() }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SophisticatedBgDark, SophisticatedSurfaceDark)
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Status & Diagnostics Header HUD
            StatusHud(
                assistantName = assistantName,
                deviceMetrics = deviceMetrics,
                aiModel = aiModel,
                onStatusClick = { onNavigate(AppScreen.PERMISSIONS) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Holographic Arc-Reactor Voice Core
            ArcReactorOrb(
                voiceState = voiceState,
                rmsLevel = rmsLevel,
                size = 200.dp,
                onClick = {
                    when (voiceState) {
                        VoiceState.IDLE -> viewModel.startListening()
                        VoiceState.LISTENING -> viewModel.stopListening()
                        VoiceState.SPEAKING -> viewModel.stopSpeaking()
                        VoiceState.EXECUTING, VoiceState.PROCESSING -> viewModel.cancelListening()
                        VoiceState.ERROR -> viewModel.startListening()
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Voice Status Badge
            VoiceStatusBadge(voiceState = voiceState)

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Real-time Audio Waveform Equalizer
            AudioWaveformVisualizer(
                rmsLevel = rmsLevel,
                voiceState = voiceState,
                maxHeight = 36.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Assistant Live Response & Speech Bubble
            SpeechBubble(
                voiceState = voiceState,
                transcription = partialText,
                assistantResponse = spokenText,
                onReplay = { viewModel.speak(spokenText) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Active Task Execution Pipeline Card
            activeTask?.let { task ->
                TaskProgressCard(
                    taskPlan = task,
                    onCancel = { viewModel.cancelListening() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 6. Quick Action Suggestion Chips
            Text(
                text = "SUGGESTED PROTOCOLS",
                color = SophisticatedPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            val suggestedCommands = listOf(
                "Open YouTube",
                "Turn on flashlight",
                "Study mode",
                "Read my notifications",
                "Set volume to 80%",
                "Good morning",
                "Search maps for coffee",
                "Take a note buy groceries"
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestedCommands.forEach { cmd ->
                    QuickActionChip(
                        title = cmd,
                        onClick = { viewModel.injectTestCommand(cmd) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 7. Manual Command Input Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SophisticatedCardBg)
                    .border(1.dp, SophisticatedCardBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = manualInputText,
                    onValueChange = { manualInputText = it },
                    placeholder = { Text("Type command or query...", color = SophisticatedTextMuted, fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = SophisticatedTextPrimary,
                        unfocusedTextColor = SophisticatedTextPrimary,
                        cursorColor = SophisticatedPrimary
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (manualInputText.isNotBlank()) {
                            viewModel.processUserQuery(manualInputText)
                            manualInputText = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = SophisticatedPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun VoiceStatusBadge(voiceState: VoiceState) {
    val (statusLabel, statusColor) = when (voiceState) {
        VoiceState.IDLE -> Pair("TAP CORE OR SPEAK", SophisticatedPrimary)
        VoiceState.LISTENING -> Pair("LISTENING TO AUDIO...", SophisticatedAccentGreen)
        VoiceState.PROCESSING -> Pair("ANALYZING NEURAL INTENT...", SophisticatedAccentPurple)
        VoiceState.EXECUTING -> Pair("EXECUTING AUTOMATION...", SophisticatedAccentAmber)
        VoiceState.SPEAKING -> Pair("JARVIS RESPONDING...", SophisticatedSecondary)
        VoiceState.ERROR -> Pair("SYSTEM INTERRUPT", SophisticatedAccentRed)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SophisticatedTertiaryContainer)
            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = statusLabel,
            color = statusColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun SpeechBubble(
    voiceState: VoiceState,
    transcription: String,
    assistantResponse: String,
    onReplay: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SophisticatedCardBg)
            .border(1.dp, SophisticatedCardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            if (voiceState == VoiceState.LISTENING && transcription.isNotBlank()) {
                Text(
                    text = "HEARD INPUT",
                    color = SophisticatedAccentGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"$transcription\"",
                    color = SophisticatedTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RESPONSE",
                    color = SophisticatedPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                IconButton(
                    onClick = onReplay,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speak Response",
                        tint = SophisticatedPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = assistantResponse,
                color = SophisticatedTextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun QuickActionChip(
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SophisticatedTertiaryContainer)
            .border(1.dp, SophisticatedCardBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = SophisticatedTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
