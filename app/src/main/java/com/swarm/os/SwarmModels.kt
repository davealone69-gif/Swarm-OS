package com.swarm.os

enum class SwarmTopology(val label: String) {
    SEQUENTIAL("Sequential"),
    PARALLEL("Parallel"),
    DEBATE("Debate"),
    HIERARCHICAL("Hierarchical"),
    BUILDER_CRITIC("Builder-Critic")
}

data class SwarmAgent(
    val id: String = System.currentTimeMillis().toString() + (0..999).random(),
    var name: String,
    var role: String,
    var modelId: String,
    var systemPrompt: String
)

data class LlmModel(
    val id: String,
    val name: String,
    val provider: String,
    val description: String = ""
)

data class SkillEntry(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val description: String,
    val promptSnippet: String,
    var timesApplied: Int = 0
)

enum class BuildScore { SUCCESS, PARTIAL, FAIL }

data class BuildTrajectory(
    val id: String = System.currentTimeMillis().toString(),
    val task: String,
    val agentRoles: List<String>,
    val steps: List<String>,
    val score: BuildScore,
    val quality: Int,
    val notes: String = ""
)

object SwarmDefaults {
    val freeModels = listOf(
        LlmModel("llama3-8b-8192", "Llama 3 8B", "Groq", "Fast free tier"),
        LlmModel("llama3-70b-8192", "Llama 3 70B", "Groq", "Strong reasoning"),
        LlmModel("gemma2-9b-it", "Gemma 2 9B", "Groq", "Google open model"),
        LlmModel("mixtral-8x7b-32768", "Mixtral 8x7B", "Groq", "Mixture of Experts"),
        LlmModel("openrouter/free", "OpenRouter Free", "OpenRouter", "Rotating free models"),
        LlmModel("simulate", "Simulate (offline)", "Local", "No API key needed")
    )

    fun defaultAgents() = mutableListOf(
        SwarmAgent(
            name = "Planner",
            role = "Planner",
            modelId = "llama3-8b-8192",
            systemPrompt = "You are a senior software planner. Break the building task into clear, ordered steps. Output only the plan."
        ),
        SwarmAgent(
            name = "Coder",
            role = "Coder",
            modelId = "llama3-70b-8192",
            systemPrompt = "You are an expert Kotlin/Jetpack Compose developer. Write clean, compilable code for the given plan. Prefer Material 3 and modern Android patterns."
        ),
        SwarmAgent(
            name = "Critic",
            role = "Reviewer",
            modelId = "mixtral-8x7b-32768",
            systemPrompt = "You are a strict code reviewer. Find bugs, missing imports, state issues, and suggest concrete fixes. Be concise."
        )
    )

    val sampleSkills = listOf(
        SkillEntry(
            title = "Compose State Hoisting",
            description = "Always lift state to the lowest common parent and pass events down.",
            promptSnippet = "When writing Compose UI, hoist mutable state to the caller. Use (value, onValueChange) pattern. Never keep business state inside leaf composables."
        ),
        SkillEntry(
            title = "Room Entity Checklist",
            description = "Ensure @Entity, primary key, and DAO methods are complete before coding.",
            promptSnippet = "For Room: 1) Define @Entity with @PrimaryKey 2) Create @Dao interface 3) Build @Database 4) Only then write usage code."
        ),
        SkillEntry(
            title = "Scaffold + TopAppBar Pattern",
            description = "Standard Material 3 screen shell used in successful builds.",
            promptSnippet = "Always start screens with Scaffold(topBar = { TopAppBar(...) }) { padding -> ... }. Apply paddingValues to the content root."
        )
    )

    val sampleTrajectories = listOf(
        BuildTrajectory(
            task = "Create a Jetpack Compose settings screen",
            agentRoles = listOf("Planner", "Coder", "Reviewer"),
            steps = listOf(
                "Planner broke task into 4 components",
                "Coder generated Scaffold + Switch preferences",
                "Reviewer fixed state hoisting issue",
                "Final compile succeeded"
            ),
            score = BuildScore.SUCCESS,
            quality = 92,
            notes = "Clean separation of concerns"
        ),
        BuildTrajectory(
            task = "Add dark theme toggle",
            agentRoles = listOf("Coder", "Tester"),
            steps = listOf(
                "Coder added isDark state",
                "Missed MaterialTheme propagation",
                "Partial UI update only"
            ),
            score = BuildScore.PARTIAL,
            quality = 61,
            notes = "Needs better theme cascade understanding"
        ),
        BuildTrajectory(
            task = "Implement Room database for logs",
            agentRoles = listOf("Planner", "Coder"),
            steps = listOf(
                "Planner skipped Entity definition",
                "Coder produced incomplete DAO",
                "Build failed on missing annotations"
            ),
            score = BuildScore.FAIL,
            quality = 28,
            notes = "Agents need stronger Room skill"
        )
    )
}
