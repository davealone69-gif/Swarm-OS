package com.swarm.os

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var inputTask: EditText
    private lateinit var spinnerTopology: Spinner
    private lateinit var agentsContainer: LinearLayout
    private lateinit var outputLog: TextView
    private lateinit var btnRun: MaterialButton

    private val agents = SwarmDefaults.defaultAgents.toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputTask = findViewById(R.id.input_task)
        spinnerTopology = findViewById(R.id.spinner_topology)
        agentsContainer = findViewById(R.id.agents_container)
        outputLog = findViewById(R.id.output_log)
        btnRun = findViewById(R.id.btn_run_swarm)

        setupTopologySpinner()
        renderAgents()
        btnRun.setOnClickListener { runSwarm() }
    }

    private fun setupTopologySpinner() {
        val labels = SwarmTopology.entries.map { it.label }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
        spinnerTopology.adapter = adapter
        // Default to Builder-Critic
        spinnerTopology.setSelection(SwarmTopology.BUILDER_CRITIC.ordinal)
    }

    private fun renderAgents() {
        agentsContainer.removeAllViews()
        agents.forEach { agent ->
            agentsContainer.addView(buildAgentCard(agent))
        }
    }

    private fun buildAgentCard(agent: SwarmAgent): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#161B22"))
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(10) }
        }

        val header = TextView(this).apply {
            text = "${agent.name}  ·  ${agent.role}"
            setTextColor(Color.parseColor("#58A6FF"))
            textSize = 15f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val model = TextView(this).apply {
            text = "model: ${agent.modelId}"
            setTextColor(Color.parseColor("#8B949E"))
            textSize = 12f
            setPadding(0, dp(2), 0, dp(8))
        }

        val prompt = TextView(this).apply {
            text = agent.systemPrompt
            setTextColor(Color.parseColor("#E6EDF3"))
            textSize = 13f
            setLineSpacing(0f, 1.15f)
        }

        card.addView(header)
        card.addView(model)
        card.addView(prompt)
        return card
    }

    private fun runSwarm() {
        val task = inputTask.text?.toString()?.trim().orEmpty()
        if (task.isEmpty()) {
            Toast.makeText(this, "Enter a build task first", Toast.LENGTH_SHORT).show()
            return
        }

        val topology = SwarmTopology.entries[spinnerTopology.selectedItemPosition]
        val log = StringBuilder()

        log.appendLine("══ Swarm run ══")
        log.appendLine("Topology: ${topology.label}")
        log.appendLine("Task: $task")
        log.appendLine()

        when (topology) {
            SwarmTopology.SEQUENTIAL,
            SwarmTopology.BUILDER_CRITIC -> {
                // Planner → Coder → Critic
                agents.forEach { agent ->
                    log.appendLine("[${agent.name}]")
                    log.appendLine(simulateReply(agent, task))
                    log.appendLine()
                }
            }
            SwarmTopology.PARALLEL -> {
                log.appendLine("Agents running in parallel…")
                log.appendLine()
                agents.forEach { agent ->
                    log.appendLine("[${agent.name}] ${simulateReply(agent, task).take(120)}…")
                }
            }
            SwarmTopology.DEBATE -> {
                val coder = agents.find { it.role == "Coder" } ?: agents[1]
                val critic = agents.find { it.role == "Reviewer" } ?: agents[2]
                log.appendLine("[${coder.name}]")
                log.appendLine(simulateReply(coder, task))
                log.appendLine()
                log.appendLine("[${critic.name}]")
                log.appendLine(simulateReply(critic, task))
                log.appendLine()
                log.appendLine("[${coder.name} — revision]")
                log.appendLine("Revised after critic notes. State hoisted; Scaffold padding applied.")
            }
            SwarmTopology.HIERARCHICAL -> {
                val planner = agents.find { it.role == "Planner" } ?: agents[0]
                log.appendLine("[${planner.name} — orchestrator]")
                log.appendLine(simulateReply(planner, task))
                log.appendLine()
                agents.filter { it.role != "Planner" }.forEach { agent ->
                    log.appendLine("[${agent.name}]")
                    log.appendLine(simulateReply(agent, task))
                    log.appendLine()
                }
            }
        }

        log.appendLine("── end ──")
        outputLog.text = log.toString()
    }

    private fun simulateReply(agent: SwarmAgent, task: String): String {
        return when (agent.role) {
            "Planner" -> """
                Plan for "$task":
                1. Define UI state
                2. Scaffold + TopAppBar
                3. Preference / content items
                4. Persist where needed
                5. Wire preview / verify
            """.trimIndent()
            "Coder" -> """
                @Composable
                fun GeneratedScreen(...) {
                  var state by remember { mutableStateOf(false) }
                  Scaffold(topBar = { TopAppBar(title = { Text("$task") }) }) { padding ->
                    Column(Modifier.padding(padding)) {
                      // implementation for: $task
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
            else -> "[${agent.name}] processed: $task"
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
