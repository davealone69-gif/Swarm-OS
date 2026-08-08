package com.swarm.os

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private val boardColumns = listOf(
        "Inbox / New" to 0,
        "Triaged" to 0,
        "Agent Assigned" to 0,
        "In Progress" to 0,
        "Blocked" to 0,
        "Review / QA" to 0,
        "Done / Released" to 0,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        populateBoard()
        wireButtons()
    }

    private fun populateBoard() {
        val container = findViewById<LinearLayout>(R.id.board_container)
        container.removeAllViews()

        boardColumns.forEach { (name, count) ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(Color.parseColor("#161B22"))
                setPadding(dp(16))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(8)
                }
            }

            val label = TextView(this).apply {
                text = name
                setTextColor(Color.parseColor("#E6EDF3"))
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val badge = TextView(this).apply {
                text = count.toString()
                setTextColor(Color.parseColor("#8B949E"))
                textSize = 14f
                gravity = Gravity.END
            }

            row.addView(label)
            row.addView(badge)
            container.addView(row)
        }
    }

    private fun wireButtons() {
        findViewById<MaterialButton>(R.id.btn_new_agent).setOnClickListener {
            toast("New Agent — coming soon")
        }
        findViewById<MaterialButton>(R.id.btn_new_project).setOnClickListener {
            toast("New Project — coming soon")
        }
        findViewById<MaterialButton>(R.id.btn_report_bug).setOnClickListener {
            toast("Report Bug — coming soon")
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
