package com.example.core.ai

class HybridAIPlanner(
    private val geminiProvider: GeminiAIProvider
) {
    suspend fun planQuery(query: String, recentContext: String = ""): AIResponse {
        // Step 1: Fast local deterministic planner
        val localResponse = RuleBasedAIPlanner.planFromQuery(query)
        if (localResponse != null) {
            return localResponse
        }

        // Step 2: Advanced Cloud AI planner (Gemini)
        return geminiProvider.processQuery(query, recentContext)
    }
}
