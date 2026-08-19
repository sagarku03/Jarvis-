package com.example.core.ai

import com.example.core.model.TaskPlan

data class AIResponse(
    val spokenResponse: String,
    val taskPlan: TaskPlan? = null,
    val isAction: Boolean = false,
    val source: String = "LOCAL" // LOCAL or GEMINI
)

interface AIProvider {
    suspend fun processQuery(query: String, recentContext: String = ""): AIResponse
    suspend fun generateChatReply(prompt: String): String
}
