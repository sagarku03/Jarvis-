package com.example.core.model

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    EXECUTING,
    SPEAKING,
    ERROR;

    val displayText: String
        get() = when (this) {
            IDLE -> "SYSTEM READY"
            LISTENING -> "LISTENING..."
            PROCESSING -> "PROCESSING QUERY..."
            EXECUTING -> "EXECUTING TASK..."
            SPEAKING -> "RESPONDING..."
            ERROR -> "DIAGNOSTIC ERROR"
        }
}
