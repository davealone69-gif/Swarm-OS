package com.swarm.os

enum class SwarmTopology(val label: String) {
    SEQUENTIAL("Sequential"),
    PARALLEL("Parallel"),
    DEBATE("Debate"),
    HIERARCHICAL("Hierarchical"),
    BUILDER_CRITIC("Builder-Critic")
}

data class SwarmAgent(
    val id: String,
    val name: String,
    val role: String,
    val modelId: String,
    val systemPrompt: String
)

object SwarmDefaults {
    val defaultAgents = listOf(
        SwarmAgent(
            id = "planner",
            name = "Planner",
            role = "Planner",
            modelId = "llama3-8b-8192",
            systemPrompt = "You are a senior software planner. Break the building task into clear, ordered steps. Output only the plan."
        ),
        SwarmAgent(
            id = "coder",
            name = "Coder",
            role = "Coder",
            modelId = "llama3-70b-8192",
            systemPrompt = "You are an expert Kotlin/Jetpack Compose developer. Write clean, compilable code for the given plan. Prefer Material 3 and modern Android patterns."
        ),
        SwarmAgent(
            id = "critic",
            name = "Critic",
            role = "Reviewer",
            modelId = "mixtral-8x7b-32768",
            systemPrompt = "You are a strict code reviewer. Find bugs, missing imports, state issues, and suggest concrete fixes. Be concise."
        )
    )

    val freeModels = listOf(
        "llama3-8b-8192",
        "llama3-70b-8192",
        "gemma2-9b-it",
        "mixtral-8x7b-32768",
        "openrouter/free",
        "huggingface/zephyr",
        "local/placeholder"
    )
}
