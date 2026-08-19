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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.RoutineItem
import com.example.ui.JarvisViewModel
import com.example.ui.theme.SophisticatedAccentAmber
import com.example.ui.theme.SophisticatedAccentGreen
import com.example.ui.theme.SophisticatedAccentRed
import com.example.ui.theme.SophisticatedBgDark
import com.example.ui.theme.SophisticatedCardBg
import com.example.ui.theme.SophisticatedCardBorder
import com.example.ui.theme.SophisticatedPrimary
import com.example.ui.theme.SophisticatedPrimaryContainer
import com.example.ui.theme.SophisticatedSecondary
import com.example.ui.theme.SophisticatedTertiary
import com.example.ui.theme.SophisticatedTertiaryContainer
import com.example.ui.theme.SophisticatedTextMuted
import com.example.ui.theme.SophisticatedTextPrimary
import com.example.ui.theme.SophisticatedTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val routines by viewModel.routines.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    var newRoutineName by remember { mutableStateOf("") }
    var newTriggerPhrase by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }

    if (showAddDialog) {
        BasicAlertDialog(
            onDismissRequest = { showAddDialog = false },
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(SophisticatedCardBg)
                .border(1.dp, SophisticatedPrimary, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .background(SophisticatedCardBg)
                    .padding(20.dp)
            ) {
                Text(
                    text = "CREATE CUSTOM PROTOCOL",
                    color = SophisticatedPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = newRoutineName,
                    onValueChange = { newRoutineName = it },
                    label = { Text("Protocol Name (e.g. Workout Setup)", color = SophisticatedTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SophisticatedPrimary,
                        unfocusedBorderColor = SophisticatedCardBorder,
                        focusedTextColor = SophisticatedTextPrimary,
                        unfocusedTextColor = SophisticatedTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newTriggerPhrase,
                    onValueChange = { newTriggerPhrase = it },
                    label = { Text("Trigger Voice Phrase (e.g. start workout)", color = SophisticatedTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SophisticatedPrimary,
                        unfocusedBorderColor = SophisticatedCardBorder,
                        focusedTextColor = SophisticatedTextPrimary,
                        unfocusedTextColor = SophisticatedTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newDescription,
                    onValueChange = { newDescription = it },
                    label = { Text("Description", color = SophisticatedTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SophisticatedPrimary,
                        unfocusedBorderColor = SophisticatedCardBorder,
                        focusedTextColor = SophisticatedTextPrimary,
                        unfocusedTextColor = SophisticatedTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showAddDialog = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SophisticatedTextSecondary)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (newRoutineName.isNotBlank() && newTriggerPhrase.isNotBlank()) {
                                viewModel.addRoutine(
                                    triggerPhrase = newTriggerPhrase,
                                    name = newRoutineName,
                                    description = newDescription,
                                    stepsJson = "DEVICE_INFO|CONTROL_VOLUME"
                                )
                                newRoutineName = ""
                                newTriggerPhrase = ""
                                newDescription = ""
                                showAddDialog = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SophisticatedPrimary,
                            contentColor = SophisticatedTertiary
                        )
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

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
                        text = "WORKFLOW AUTOMATION",
                        color = SophisticatedPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Routines & Protocols",
                        color = SophisticatedTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SophisticatedPrimary,
                        contentColor = SophisticatedTertiary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Protocol", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hero Highlight Card (Sophisticated Dark feature)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(SophisticatedPrimaryContainer)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE AUTOMATIONS",
                            color = SophisticatedSecondary.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${routines.count { it.isEnabled }} Enabled",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Autonomous Chains",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Execute multi-step tasks across apps, volume, device hardware, and voice responses seamlessly.",
                        color = SophisticatedSecondary.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Routines list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(routines, key = { it.id }) { routine ->
                    RoutineCard(
                        routine = routine,
                        onToggle = { viewModel.toggleRoutine(routine) },
                        onExecute = { viewModel.executeRoutine(routine) },
                        onDelete = { viewModel.deleteRoutine(routine.id) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun RoutineCard(
    routine: RoutineItem,
    onToggle: () -> Unit,
    onExecute: () -> Unit,
    onDelete: () -> Unit
) {
    val icon = when (routine.iconName) {
        "wb_sunny" -> Icons.Default.WbSunny
        "school" -> Icons.Default.School
        "sports_esports" -> Icons.Default.SportsEsports
        else -> Icons.Default.ElectricBolt
    }

    val stepCount = routine.stepsJson.split("|").filter { it.isNotBlank() }.size

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SophisticatedCardBg)
            .border(1.dp, SophisticatedCardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
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
                            .background(SophisticatedTertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = SophisticatedPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = routine.name,
                            color = SophisticatedTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Say: \"${routine.triggerPhrase}\"",
                            color = SophisticatedPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Switch(
                    checked = routine.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SophisticatedTertiary,
                        checkedTrackColor = SophisticatedPrimary,
                        uncheckedThumbColor = SophisticatedTextMuted,
                        uncheckedTrackColor = SophisticatedTertiaryContainer
                    )
                )
            }

            if (routine.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = routine.description,
                    color = SophisticatedTextSecondary,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$stepCount chained steps",
                    color = SophisticatedTextMuted,
                    fontSize = 11.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Routine",
                            tint = SophisticatedAccentRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Button(
                        onClick = onExecute,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SophisticatedPrimary,
                            contentColor = SophisticatedTertiary
                        ),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Execute", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
