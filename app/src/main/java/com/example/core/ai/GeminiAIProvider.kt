package com.example.core.ai

import android.util.Log
import com.example.BuildConfig
import com.example.core.model.ActionStep
import com.example.core.model.ActionType
import com.example.core.model.TaskPlan
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAIProvider(
    private var customApiKey: String? = null,
    private var modelName: String = "gemini-3.5-flash"
) : AIProvider {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiKey: String
        get() {
            val custom = customApiKey?.trim()
            if (!custom.isNullOrEmpty()) return custom
            return try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }
        }

    fun updateConfig(apiKey: String?, model: String) {
        this.customApiKey = apiKey
        this.modelName = model
    }

    override suspend fun processQuery(query: String, recentContext: String): AIResponse = withContext(Dispatchers.IO) {
        val currentKey = apiKey
        if (currentKey.isBlank() || currentKey == "MY_GEMINI_API_KEY") {
            // Graceful fallback when API key is not yet set in Secrets panel
            return@withContext AIResponse(
                spokenResponse = "I have processed your query: $query.",
                taskPlan = TaskPlan(
                    userQuery = query,
                    intent = "SEARCH_WEB",
                    steps = listOf(
                        ActionStep(
                            type = ActionType.SEARCH_WEB,
                            title = "Web Intelligence",
                            description = "Searching for: $query",
                            params = mapOf("query" to query)
                        )
                    )
                ),
                isAction = true,
                source = "LOCAL"
            )
        }

        try {
            val systemInstruction = """
                You are JARVIS, an autonomous futuristic Android AI Voice Assistant.
                Respond strictly in JSON format matching this schema:
                {
                  "spokenResponse": "Short, concise voice response (maximum 1-2 sentences).",
                  "isAction": true/false,
                  "intent": "OPEN_APP|SEARCH_APP|SEARCH_WEB|CONTROL_VOLUME|FLASHLIGHT_CONTROL|SET_ALARM|SET_TIMER|CREATE_REMINDER|CREATE_NOTE|MAKE_CALL|DRAFT_MESSAGE|READ_NOTIFICATIONS|PRESS_BACK|PRESS_HOME|TAKE_SCREENSHOT|OPEN_SETTINGS|DEVICE_INFO|AI_CONVERSATION",
                  "actionType": "OPEN_APP|SEARCH_APP|SEARCH_WEB|CONTROL_VOLUME|FLASHLIGHT_CONTROL|SET_ALARM|SET_TIMER|CREATE_REMINDER|CREATE_NOTE|MAKE_CALL|DRAFT_MESSAGE|READ_NOTIFICATIONS|PRESS_BACK|PRESS_HOME|TAKE_SCREENSHOT|OPEN_SETTINGS|DEVICE_INFO|AI_CONVERSATION",
                  "parameters": {
                     "key": "value"
                  }
                }
                If the query is a simple question or chat, set isAction to false and give a concise, intelligent JARVIS reply in spokenResponse.
                Never include backticks or markdown, output pure JSON only.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            if (recentContext.isNotBlank()) {
                                put(JSONObject().put("text", "Context: $recentContext\nUser: $query"))
                            } else {
                                put(JSONObject().put("text", query))
                            }
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemInstruction))
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("maxOutputTokens", 500)
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$currentKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiAI", "API Error: ${response.code} $responseBody")
                return@withContext AIResponse(
                    spokenResponse = "I encountered an API error. Searching online instead.",
                    taskPlan = TaskPlan(
                        userQuery = query,
                        intent = "SEARCH_WEB",
                        steps = listOf(
                            ActionStep(type = ActionType.SEARCH_WEB, title = "Search", description = "Query: $query", params = mapOf("query" to query))
                        )
                    ),
                    isAction = true
                )
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val contentObj = candidate?.optJSONObject("content")
            val parts = contentObj?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text")?.trim() ?: ""

            val cleanedJson = rawText.replace("```json", "").replace("```", "").trim()
            val parsedResult = JSONObject(cleanedJson)

            val spoken = parsedResult.optString("spokenResponse", "Understood.")
            val isAction = parsedResult.optBoolean("isAction", false)
            val actionTypeStr = parsedResult.optString("actionType", "AI_CONVERSATION")
            val intent = parsedResult.optString("intent", actionTypeStr)
            val paramsObj = parsedResult.optJSONObject("parameters")
            val paramsMap = mutableMapOf<String, String>()
            if (paramsObj != null) {
                val keys = paramsObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    paramsMap[k] = paramsObj.optString(k)
                }
            }

            val actionType = try {
                ActionType.valueOf(actionTypeStr)
            } catch (e: Exception) {
                if (isAction) ActionType.SEARCH_WEB else ActionType.AI_CONVERSATION
            }

            val taskPlan = if (isAction) {
                TaskPlan(
                    userQuery = query,
                    intent = intent,
                    steps = listOf(
                        ActionStep(
                            type = actionType,
                            title = intent.replace("_", " "),
                            description = "Executing $intent",
                            params = paramsMap,
                            requiresConfirmation = actionType == ActionType.MAKE_CALL || actionType == ActionType.DRAFT_MESSAGE
                        )
                    )
                )
            } else null

            AIResponse(
                spokenResponse = spoken,
                taskPlan = taskPlan,
                isAction = isAction,
                source = "GEMINI"
            )

        } catch (e: Exception) {
            Log.e("GeminiAI", "Processing failed", e)
            AIResponse(
                spokenResponse = "I couldn't reach the AI network. Launching search for you.",
                taskPlan = TaskPlan(
                    userQuery = query,
                    intent = "SEARCH_WEB",
                    steps = listOf(
                        ActionStep(type = ActionType.SEARCH_WEB, title = "Web Search", description = query, params = mapOf("query" to query))
                    )
                ),
                isAction = true,
                source = "FALLBACK"
            )
        }
    }

    override suspend fun generateChatReply(prompt: String): String = withContext(Dispatchers.IO) {
        val currentKey = apiKey
        if (currentKey.isBlank() || currentKey == "MY_GEMINI_API_KEY") {
            return@withContext "JARVIS protocol active. Ready to execute your phone commands."
        }
        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$currentKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            val json = JSONObject(responseBody)
            val raw = json.optJSONArray("candidates")?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
            raw?.trim() ?: "Understood."
        } catch (e: Exception) {
            "I'm currently running in offline mode."
        }
    }
}
