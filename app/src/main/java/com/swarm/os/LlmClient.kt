package com.swarm.os

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Thin client for free LLM endpoints.
 * - Groq: set GROQ_API_KEY in SharedPreferences (key = "groq_api_key")
 * - OpenRouter: set OPENROUTER_API_KEY (key = "openrouter_api_key")
 * Falls back to simulation when no key or model is "simulate".
 */
object LlmClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    var groqApiKey: String = ""
    var openRouterApiKey: String = ""

    fun chat(modelId: String, systemPrompt: String, userMessage: String): String {
        if (modelId == "simulate" || modelId.isBlank()) {
            return simulate(systemPrompt, userMessage)
        }

        return try {
            when {
                modelId.startsWith("openrouter") || modelId.contains("openrouter") ->
                    callOpenRouter(modelId, systemPrompt, userMessage)
                else ->
                    callGroq(modelId, systemPrompt, userMessage)
            }
        } catch (e: Exception) {
            "[API error: ${e.message}]\n\nFalling back to simulation:\n${simulate(systemPrompt, userMessage)}"
        }
    }

    private fun callGroq(modelId: String, system: String, user: String): String {
        if (groqApiKey.isBlank()) {
            return "[No Groq API key set — using simulation]\n\n${simulate(system, user)}"
        }
        val body = JSONObject().apply {
            put("model", modelId)
            put("messages", JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", system))
                put(JSONObject().put("role", "user").put("content", user))
            })
            put("temperature", 0.4)
            put("max_tokens", 1024)
        }
        val req = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader("Authorization", "Bearer $groqApiKey")
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(req).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) return "[Groq ${resp.code}] $raw"
            val json = JSONObject(raw)
            return json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }
    }

    private fun callOpenRouter(modelId: String, system: String, user: String): String {
        if (openRouterApiKey.isBlank()) {
            return "[No OpenRouter API key set — using simulation]\n\n${simulate(system, user)}"
        }
        val model = if (modelId == "openrouter/free") "openrouter/auto" else modelId
        val body = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", system))
                put(JSONObject().put("role", "user").put("content", user))
            })
        }
        val req = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .addHeader("Authorization", "Bearer $openRouterApiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://github.com/davealone69-gif/Swarm-OS")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(req).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) return "[OpenRouter ${resp.code}] $raw"
            val json = JSONObject(raw)
            return json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }
    }

    private fun simulate(system: String, user: String): String {
        val roleHint = when {
            system.contains("planner", true) -> "Planner"
            system.contains("coder", true) || system.contains("developer", true) -> "Coder"
            system.contains("reviewer", true) || system.contains("critic", true) -> "Reviewer"
            else -> "Agent"
        }
        return when (roleHint) {
            "Planner" -> """
                Plan for "$user":
                1. Define UI state
                2. Scaffold + TopAppBar
                3. Content / preference items
                4. Persist where needed
                5. Wire preview / verify
            """.trimIndent()
            "Coder" -> """
                @Composable
                fun GeneratedScreen(...) {
                  var state by remember { mutableStateOf(false) }
                  Scaffold(topBar = { TopAppBar(title = { Text("$user") }) }) { padding ->
                    Column(Modifier.padding(padding)) {
                      // implementation for: $user
                    }
                  }
                }
            """.trimIndent()
            "Reviewer" -> """
                Review notes:
                - Prefer collectAsState with ViewModel
                - Extract reusable row composables
                - Handle system dark mode fallback
                - Check missing imports
            """.trimIndent()
            else -> "[$roleHint] processed: $user\n(System: ${system.take(80)}…)"
        }
    }
}
