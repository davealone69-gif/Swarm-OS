package com.swarm.os

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var tabBuilder: TextView
    private lateinit var tabTraining: TextView
    private lateinit var tabSettings: TextView
    private lateinit var panelBuilder: View
    private lateinit var panelTraining: View
    private lateinit var panelSettings: View

    private lateinit var inputTask: EditText
    private lateinit var spinnerTopology: Spinner
    private lateinit var agentsContainer: LinearLayout
    private lateinit var outputLog: TextView
    private lateinit var btnRun: MaterialButton
    private lateinit var btnAddAgent: MaterialButton

    private lateinit var skillsContainer: LinearLayout
    private lateinit var trajectoriesContainer: LinearLayout

    private lateinit var inputGroq: EditText
    private lateinit var inputOpenRouter: EditText

    private val agents = SwarmDefaults.defaultAgents()
    private val skills = SwarmDefaults.sampleSkills.toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        loadKeys()
        setupTabs()
        setupTopology()
        renderAgents()
        renderTraining()

        btnAddAgent.setOnClickListener { showAddAgentDialog() }
        btnRun.setOnClickListener { runSwarm() }
        findViewById<MaterialButton>(R.id.btn_save_keys).setOnClickListener { saveKeys() }
    }

    private fun bindViews() {
        tabBuilder = findViewById(R.id.tab_builder)
        tabTraining = findViewById(R.id.tab_training)
        tabSettings = findViewById(R.id.tab_settings)
        panelBuilder = findViewById(R.id.panel_builder)
        panelTraining = findViewById(R.id.panel_training)
        panelSettings = findViewById(R.id.panel_settings)

        inputTask = findViewById(R.id.input_task)
        spinnerTopology = findViewById(R.id.spinner_topology)
        agentsContainer = findViewById(R.id.agents_container)
        outputLog = findViewById(R.id.output_log)
        btnRun = findViewById(R.id.btn_run_swarm)
        btnAddAgent = findViewById(R.id.btn_add_agent)

        skillsContainer = findViewById(R.id.skills_container)
        trajectoriesContainer = findViewById(R.id.trajectories_container)

        inputGroq = findViewById(R.id.input_groq_key)
        inputOpenRouter = findViewById(R.id.input_openrouter_key)
    }

    // ── Tabs ──────────────────────────────────────────────────────────

    private fun setupTabs() {
        tabBuilder.setOnClickListener { showPanel(0) }
        tabTraining.setOnClickListener { showPanel(1) }
        tabSettings.setOnClickListener { showPanel(2) }
        showPanel(0)
    }

    private fun showPanel(index: Int) {
        panelBuilder.visibility = if (index == 0) View.VISIBLE else View.GONE
        panelTraining.visibility = if (index == 1) View.VISIBLE else View.GONE
        panelSettings.visibility = if (index == 2) View.VISIBLE else View.GONE
        tabBuilder.setTextColor(Color.parseColor(if (index == 0) "#58A6FF" else "#8B949E"))
        tabTraining.setTextColor(Color.parseColor(if (index == 1) "#58A6FF" else "#8B949E"))
        tabSettings.setTextColor(Color.parseColor(if (index == 2) "#58A6FF" else "#8B949E"))
        if (index == 0) tabBuilder.setTypeface(null, android.graphics.Typeface.BOLD) else tabBuilder.setTypeface(null, android.graphics.Typeface.NORMAL)
        if (index == 1) tabTraining.setTypeface(null, android.graphics.Typeface.BOLD) else tabTraining.setTypeface(null, android.graphics.Typeface.NORMAL)
        if (index == 2) tabSettings.setTypeface(null, android.graphics.Typeface.BOLD) else tabSettings.setTypeface(null, android.graphics.Typeface.NORMAL)
    }

    // ── Topology ──────────────────────────────────────────────────────

    private fun setupTopology() {
        val labels = SwarmTopology.entries.map { it.label }
        spinnerTopology.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
        spinnerTopology.setSelection(SwarmTopology.BUILDER_CRITIC.ordinal)
    }

    // ── Agents ────────────────────────────────────────────────────────

    private fun renderAgents() {
        agentsContainer.removeAllViews()
        agents.forEachIndexed { index, agent ->
            agentsContainer.addView(buildAgentCard(agent, index))
        }
    }

    private fun buildAgentCard(agent: SwarmAgent, index: Int): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#161B22"))
            setPadding(dp(12), dp(12), dp(12), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(8) }
        }

        // Header row: name + edit + delete
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val title = TextView(this).apply {
            text = "${agent.name}  ·  ${agent.role}"
            setTextColor(Color.parseColor("#58A6FF"))
            textSize = 14f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val btnEdit = TextView(this).apply {
            text = "Edit"
            setTextColor(Color.parseColor("#58A6FF"))
            textSize = 13f
            setPadding(dp(8), 0, dp(8), 0)
            setOnClickListener { showEditAgentDialog(agent, index) }
        }

        val btnDel = TextView(this).apply {
            text = "✕"
            setTextColor(Color.parseColor("#F85149"))
            textSize = 14f
            setPadding(dp(8), 0, 0, 0)
            setOnClickListener {
                agents.removeAt(index)
                renderAgents()
            }
        }

        header.addView(title)
        header.addView(btnEdit)
        header.addView(btnDel)

        val model = TextView(this).apply {
            text = "model: ${agent.modelId}"
            setTextColor(Color.parseColor("#8B949E"))
            textSize = 11f
            setPadding(0, dp(2), 0, dp(6))
        }

        val prompt = TextView(this).apply {
            text = agent.systemPrompt
            setTextColor(Color.parseColor("#E6EDF3"))
            textSize = 12f
            setLineSpacing(0f, 1.15f)
            maxLines = 4
        }

        card.addView(header)
        card.addView(model)
        card.addView(prompt)
        return card
    }

    private fun showAddAgentDialog() {
        showAgentDialog(null, -1)
    }

    private fun showEditAgentDialog(agent: SwarmAgent, index: Int) {
        showAgentDialog(agent, index)
    }

    private fun showAgentDialog(existing: SwarmAgent?, index: Int) {
        val pad = dp(16)
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, 0)
        }

        fun labeledField(hint: String, value: String, singleLine: Boolean = true): EditText {
            val label = TextView(this).apply {
                text = hint.uppercase()
                setTextColor(Color.parseColor("#8B949E"))
                textSize = 11f
            }
            val field = EditText(this).apply {
                setText(value)
                setTextColor(Color.parseColor("#E6EDF3"))
                setHintTextColor(Color.parseColor("#484F58"))
                setBackgroundColor(Color.parseColor("#21262D"))
                setPadding(dp(10), dp(10), dp(10), dp(10))
                textSize = 14f
                if (!singleLine) {
                    minLines = 3
                    gravity = android.view.Gravity.TOP or android.view.Gravity.START
                    inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                }
            }
            form.addView(label)
            form.addView(field)
            form.addView(View(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(10))
            })
            return field
        }

        val nameField = labeledField("Name", existing?.name ?: "")
        val roleField = labeledField("Role", existing?.role ?: "Coder")
        val modelField = labeledField("Model ID", existing?.modelId ?: "simulate")
        val promptField = labeledField(
            "System Prompt",
            existing?.systemPrompt ?: "You are an expert Android developer. Write clean Kotlin and Jetpack Compose code.",
            singleLine = false
        )

        // Model picker helper text
        form.addView(TextView(this).apply {
            text = "Models: llama3-8b-8192 · llama3-70b-8192 · gemma2-9b-it · mixtral-8x7b-32768 · openrouter/free · simulate"
            setTextColor(Color.parseColor("#484F58"))
            textSize = 11f
            setPadding(0, 0, 0, dp(8))
        })

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Add Agent" else "Edit Agent")
            .setView(form)
            .setPositiveButton(if (existing == null) "Add" else "Save") { _, _ ->
                val name = nameField.text.toString().trim().ifEmpty { "Agent" }
                val role = roleField.text.toString().trim().ifEmpty { "Coder" }
                val model = modelField.text.toString().trim().ifEmpty { "simulate" }
                val prompt = promptField.text.toString().trim().ifEmpty {
                    "You are an expert Android developer. Write clean Kotlin and Jetpack Compose code."
                }
                if (existing == null) {
                    agents.add(SwarmAgent(name = name, role = role, modelId = model, systemPrompt = prompt))
                } else {
                    existing.name = name
                    existing.role = role
                    existing.modelId = model
                    existing.systemPrompt = prompt
                }
                renderAgents()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Run swarm ─────────────────────────────────────────────────────

    private fun runSwarm() {
        val task = inputTask.text?.toString()?.trim().orEmpty()
        if (task.isEmpty()) {
            Toast.makeText(this, "Enter a build task first", Toast.LENGTH_SHORT).show()
            return
        }
        if (agents.isEmpty()) {
            Toast.makeText(this, "Add at least one agent", Toast.LENGTH_SHORT).show()
            return
        }

        val topology = SwarmTopology.entries[spinnerTopology.selectedItemPosition]
        btnRun.isEnabled = false
        btnRun.text = "Running…"
        outputLog.text = "Starting swarm…\nTopology: ${topology.label}\nTask: $task\n"

        // Inject skill snippets into coder prompt if present
        val skillBoost = skills.joinToString("\n") { "- ${it.promptSnippet}" }

        thread {
            val log = StringBuilder()
            log.appendLine("══ Swarm run ══")
            log.appendLine("Topology: ${topology.label}")
            log.appendLine("Task: $task")
            log.appendLine()

            fun call(agent: SwarmAgent, extra: String = ""): String {
                var sys = agent.systemPrompt
                if (agent.role.contains("Coder", true) && skillBoost.isNotBlank()) {
                    sys += "\n\nSkill library:\n$skillBoost"
                }
                return LlmClient.chat(agent.modelId, sys, "$task\n$extra")
            }

            when (topology) {
                SwarmTopology.SEQUENTIAL, SwarmTopology.BUILDER_CRITIC -> {
                    var context = ""
                    agents.forEach { agent ->
                        runOnUiThread { outputLog.text = log.toString() + "\n[${agent.name}] thinking…" }
                        val reply = call(agent, context)
                        log.appendLine("[${agent.name}]")
                        log.appendLine(reply)
                        log.appendLine()
                        context += "\n[${agent.name}]: $reply"
                    }
                }
                SwarmTopology.PARALLEL -> {
                    agents.forEach { agent ->
                        val reply = call(agent)
                        log.appendLine("[${agent.name}]")
                        log.appendLine(reply.take(300) + if (reply.length > 300) "…" else "")
                        log.appendLine()
                    }
                }
                SwarmTopology.DEBATE -> {
                    val coder = agents.find { it.role.contains("Coder", true) } ?: agents.first()
                    val critic = agents.find { it.role.contains("Review", true) || it.role.contains("Critic", true) }
                        ?: agents.last()
                    val draft = call(coder)
                    log.appendLine("[${coder.name}]")
                    log.appendLine(draft)
                    log.appendLine()
                    val critique = call(critic, "Review this draft:\n$draft")
                    log.appendLine("[${critic.name}]")
                    log.appendLine(critique)
                    log.appendLine()
                    val revision = call(coder, "Revise after critic notes:\n$critique\n\nOriginal:\n$draft")
                    log.appendLine("[${coder.name} — revision]")
                    log.appendLine(revision)
                }
                SwarmTopology.HIERARCHICAL -> {
                    val planner = agents.find { it.role.contains("Plan", true) } ?: agents.first()
                    val plan = call(planner)
                    log.appendLine("[${planner.name} — orchestrator]")
                    log.appendLine(plan)
                    log.appendLine()
                    agents.filter { it.id != planner.id }.forEach { agent ->
                        val reply = call(agent, "Follow this plan:\n$plan")
                        log.appendLine("[${agent.name}]")
                        log.appendLine(reply)
                        log.appendLine()
                    }
                }
            }

            log.appendLine("── end ──")
            runOnUiThread {
                outputLog.text = log.toString()
                btnRun.isEnabled = true
                btnRun.text = "▶  Run Swarm"
            }
        }
    }

    // ── Training Centre ───────────────────────────────────────────────

    private fun renderTraining() {
        skillsContainer.removeAllViews()
        skills.forEach { skill ->
            skillsContainer.addView(skillCard(skill))
        }
        trajectoriesContainer.removeAllViews()
        SwarmDefaults.sampleTrajectories.forEach { t ->
            trajectoriesContainer.addView(trajectoryCard(t))
        }
    }

    private fun skillCard(skill: SkillEntry): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#161B22"))
            setPadding(dp(12), dp(12), dp(12), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(8) }
        }
        card.addView(TextView(this).apply {
            text = skill.title
            setTextColor(Color.parseColor("#58A6FF"))
            textSize = 14f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        card.addView(TextView(this).apply {
            text = skill.description
            setTextColor(Color.parseColor("#8B949E"))
            textSize = 12f
            setPadding(0, dp(2), 0, dp(6))
        })
        card.addView(TextView(this).apply {
            text = skill.promptSnippet
            setTextColor(Color.parseColor("#E6EDF3"))
            textSize = 12f
            setLineSpacing(0f, 1.15f)
        })
        card.addView(TextView(this).apply {
            text = "Apply to Coder →"
            setTextColor(Color.parseColor("#3FB950"))
            textSize = 12f
            setPadding(0, dp(8), 0, 0)
            setOnClickListener {
                skill.timesApplied++
                val coder = agents.find { it.role.contains("Coder", true) }
                if (coder != null) {
                    if (!coder.systemPrompt.contains(skill.promptSnippet)) {
                        coder.systemPrompt += "\n\n${skill.promptSnippet}"
                        renderAgents()
                        Toast.makeText(this@MainActivity, "Skill applied to ${coder.name}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Already applied", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "No Coder agent found", Toast.LENGTH_SHORT).show()
                }
            }
        })
        return card
    }

    private fun trajectoryCard(t: BuildTrajectory): View {
        val color = when (t.score) {
            BuildScore.SUCCESS -> "#3FB950"
            BuildScore.PARTIAL -> "#D29922"
            BuildScore.FAIL -> "#F85149"
        }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#161B22"))
            setPadding(dp(12), dp(12), dp(12), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(8) }
        }
        card.addView(TextView(this).apply {
            text = "${t.score.name}  ·  quality ${t.quality}"
            setTextColor(Color.parseColor(color))
            textSize = 12f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        card.addView(TextView(this).apply {
            text = t.task
            setTextColor(Color.parseColor("#E6EDF3"))
            textSize = 14f
            setPadding(0, dp(4), 0, dp(4))
        })
        card.addView(TextView(this).apply {
            text = t.steps.joinToString(" → ")
            setTextColor(Color.parseColor("#8B949E"))
            textSize = 11f
        })
        if (t.notes.isNotBlank()) {
            card.addView(TextView(this).apply {
                text = t.notes
                setTextColor(Color.parseColor("#8B949E"))
                textSize = 11f
                setPadding(0, dp(4), 0, 0)
            })
        }
        return card
    }

    // ── API keys ──────────────────────────────────────────────────────

    private fun prefs() = getSharedPreferences("swarm_os", Context.MODE_PRIVATE)

    private fun loadKeys() {
        val p = prefs()
        val groq = p.getString("groq_api_key", "").orEmpty()
        val or = p.getString("openrouter_api_key", "").orEmpty()
        inputGroq.setText(groq)
        inputOpenRouter.setText(or)
        LlmClient.groqApiKey = groq
        LlmClient.openRouterApiKey = or
    }

    private fun saveKeys() {
        val groq = inputGroq.text?.toString()?.trim().orEmpty()
        val or = inputOpenRouter.text?.toString()?.trim().orEmpty()
        prefs().edit()
            .putString("groq_api_key", groq)
            .putString("openrouter_api_key", or)
            .apply()
        LlmClient.groqApiKey = groq
        LlmClient.openRouterApiKey = or
        Toast.makeText(this, "Keys saved on device", Toast.LENGTH_SHORT).show()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
