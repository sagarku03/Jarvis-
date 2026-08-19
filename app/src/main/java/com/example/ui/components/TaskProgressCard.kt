package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.ActionStep
import com.example.core.model.TaskPlan
import com.example.core.model.TaskState
import com.example.ui.theme.SophisticatedAccentAmber
import com.example.ui.theme.SophisticatedAccentGreen
import com.example.ui.theme.SophisticatedAccentRed
import com.example.ui.theme.SophisticatedCardBg
import com.example.ui.theme.SophisticatedCardBorder
import com.example.ui.theme.SophisticatedPrimary
import com.example.ui.theme.SophisticatedTertiaryContainer
import com.example.ui.theme.SophisticatedTextPrimary
import com.example.ui.theme.SophisticatedTextSecondary

@Composable
fun TaskProgressCard(
    taskPlan: TaskPlan,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SophisticatedCardBg)
            .border(1.dp, SophisticatedCardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header: Intent Title & Cancel Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TASK PIPELINE",
                        color = SophisticatedPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = taskPlan.intent.replace("_", " "),
                        color = SophisticatedTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (taskPlan.state == TaskState.EXECUTING || taskPlan.state == TaskState.WAITING_CONFIRMATION) {
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SophisticatedAccentRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Abort", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { taskPlan.progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SophisticatedPrimary,
                trackColor = SophisticatedTertiaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-step Timeline
            taskPlan.steps.forEachIndexed { index, step ->
                StepTimelineRow(
                    step = step,
                    isCurrent = index == taskPlan.currentStepIndex && taskPlan.state == TaskState.EXECUTING
                )
                if (index < taskPlan.steps.size - 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun StepTimelineRow(
    step: ActionStep,
    isCurrent: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isCurrent) SophisticatedTertiaryContainer else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (step.isCompleted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SophisticatedAccentGreen,
                modifier = Modifier.size(18.dp)
            )
        } else if (isCurrent) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = SophisticatedPrimary,
                strokeWidth = 2.dp
            )
        } else if (step.errorMessage != null) {
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = null,
                tint = SophisticatedAccentRed,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.HourglassEmpty,
                contentDescription = null,
                tint = SophisticatedTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.title,
                color = if (isCurrent) SophisticatedPrimary else SophisticatedTextPrimary,
                fontSize = 13.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
            )
            Text(
                text = step.statusMessage,
                color = if (step.errorMessage != null) SophisticatedAccentRed else SophisticatedTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}
