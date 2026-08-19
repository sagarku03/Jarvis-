package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JarvisViewModel
import com.example.ui.theme.SophisticatedAccentAmber
import com.example.ui.theme.SophisticatedAccentGreen
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val assistantName by viewModel.assistantName.collectAsState()
    val aiProviderMode by viewModel.aiProviderMode.collectAsState()
    val floatingOverlayEnabled by viewModel.floatingOverlayEnabled.collectAsState()

    var nameInput by remember { mutableStateOf(assistantName) }
    var apiKeyInput by remember { mutableStateOf("") }
    var speechRate by remember { mutableFloatStateOf(1.05f) }
    var speechPitch by remember { mutableFloatStateOf(0.95f) }

    var isModelMenuOpen by remember { mutableStateOf(false) }
    val availableModels = listOf("gemini-3.5-flash", "gemini-1.5-flash", "gemini-1.5-pro", "Local Offline Only")

    val scrollState = rememberScrollState()

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
            Text(
                text = "SYSTEM CONFIGURATION",
                color = SophisticatedPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Preferences & AI Core",
                color = SophisticatedTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Assistant Identity Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SophisticatedCardBg)
                    .border(1.dp, SophisticatedCardBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = SophisticatedPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Assistant Identity",
                            color = SophisticatedTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = {
                            nameInput = it
                            viewModel.setAssistantName(it)
                        },
                        label = { Text("Assistant Call Sign", color = SophisticatedTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SophisticatedPrimary,
                            unfocusedBorderColor = SophisticatedCardBorder,
                            focusedTextColor = SophisticatedTextPrimary,
                            unfocusedTextColor = SophisticatedTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Speech Rate: ${String.format("%.2f", speechRate)}x", color = SophisticatedTextSecondary, fontSize = 13.sp)
                    Slider(
                        value = speechRate,
                        onValueChange = {
                            speechRate = it
                            viewModel.setSpeechSettings(speechRate, speechPitch)
                        },
                        valueRange = 0.7f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = SophisticatedPrimary,
                            activeTrackColor = SophisticatedPrimary,
                            inactiveTrackColor = SophisticatedTertiaryContainer
                        )
                    )

                    Text("Voice Pitch: ${String.format("%.2f", speechPitch)}", color = SophisticatedTextSecondary, fontSize = 13.sp)
                    Slider(
                        value = speechPitch,
                        onValueChange = {
                            speechPitch = it
                            viewModel.setSpeechSettings(speechRate, speechPitch)
                        },
                        valueRange = 0.7f..1.3f,
                        colors = SliderDefaults.colors(
                            thumbColor = SophisticatedPrimary,
                            activeTrackColor = SophisticatedPrimary,
                            inactiveTrackColor = SophisticatedTertiaryContainer
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.speak("Greetings. I am ${nameInput.ifBlank { "JARVIS" }}, all systems are nominal.")
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SophisticatedTertiary,
                            contentColor = SophisticatedPrimary
                        ),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Voice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. AI Intelligence Engine Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SophisticatedCardBg)
                    .border(1.dp, SophisticatedCardBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = SophisticatedPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Neural Engine",
                            color = SophisticatedTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = isModelMenuOpen,
                        onExpandedChange = { isModelMenuOpen = !isModelMenuOpen }
                    ) {
                        OutlinedTextField(
                            value = aiProviderMode,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Model Architecture", color = SophisticatedTextSecondary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isModelMenuOpen) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SophisticatedPrimary,
                                unfocusedBorderColor = SophisticatedCardBorder,
                                focusedTextColor = SophisticatedTextPrimary,
                                unfocusedTextColor = SophisticatedTextPrimary
                            ),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isModelMenuOpen,
                            onDismissRequest = { isModelMenuOpen = false },
                            modifier = Modifier.background(SophisticatedCardBg)
                        ) {
                            availableModels.forEach { model ->
                                DropdownMenuItem(
                                    text = { Text(model, color = SophisticatedTextPrimary) },
                                    onClick = {
                                        viewModel.setAiConfig(apiKeyInput, model)
                                        isModelMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            viewModel.setAiConfig(it, aiProviderMode)
                        },
                        label = { Text("Custom Gemini API Key (Optional)", color = SophisticatedTextSecondary) },
                        placeholder = { Text("Managed via Secrets panel", color = SophisticatedTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SophisticatedPrimary,
                            unfocusedBorderColor = SophisticatedCardBorder,
                            focusedTextColor = SophisticatedTextPrimary,
                            unfocusedTextColor = SophisticatedTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Floating Overlay Service Toggle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SophisticatedCardBg)
                    .border(1.dp, SophisticatedCardBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
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
                        Icon(Icons.Default.Layers, contentDescription = null, tint = SophisticatedPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Floating Arc-Reactor Bubble",
                                color = SophisticatedTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Keep JARVIS one-tap accessible over all apps",
                                color = SophisticatedTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Switch(
                        checked = floatingOverlayEnabled,
                        onCheckedChange = { viewModel.toggleFloatingOverlay(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SophisticatedTertiary,
                            checkedTrackColor = SophisticatedPrimary,
                            uncheckedThumbColor = SophisticatedTextMuted,
                            uncheckedTrackColor = SophisticatedTertiaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. About JARVIS Architecture Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SophisticatedCardBg)
                    .border(1.dp, SophisticatedCardBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = SophisticatedPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "About JARVIS Core",
                            color = SophisticatedTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "JARVIS is an autonomous on-device AI voice assistant & command center. Crafted with the Sophisticated Dark design system, Room database persistence, and Google Gemini AI.",
                        color = SophisticatedTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Design: Sophisticated Dark • Build: v2.5.0-RELEASE",
                        color = SophisticatedTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
