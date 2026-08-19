package com.example.core.model

data class RoutineItem(
    val id: Long = 0,
    val triggerPhrase: String,
    val name: String,
    val description: String,
    val stepsJson: String,
    val isEnabled: Boolean = true,
    val iconName: String = "routine"
)
