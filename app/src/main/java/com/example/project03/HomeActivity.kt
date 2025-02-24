package com.example.project03

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children

class HomeActivity : AppCompatActivity() {

    private lateinit var createTaskButton: Button
    private lateinit var viewTasksButton: Button
    private lateinit var backButton: Button
    private lateinit var tasksContainer: LinearLayout

    private val handler: Handler = Handler(Looper.getMainLooper())
    private var isTimerRunning: Boolean = false
    private var currentTask: String? = null
    private var timerValue: Long = 0L
    private val taskTimers: MutableMap<String, Long> = mutableMapOf()

    private val runnable: Runnable = object : Runnable {
        override fun run() {
            if (isTimerRunning) {
                timerValue++
                currentTask?.let { updateTimerView(it) }
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize buttons
        createTaskButton = findViewById(R.id.createTaskButton)
        viewTasksButton = findViewById(R.id.viewTasksButton)
        backButton = findViewById(R.id.backButton)
        tasksContainer = findViewById(R.id.tasksContainer)

        // Set click listener for "Create Task" button
        createTaskButton.setOnClickListener {
            // Start CreateTaskActivity
            val intent = Intent(this, CreateTaskActivity::class.java)
            startActivity(intent)
        }

        // Set click listener for "View My Tasks" button
        viewTasksButton.setOnClickListener {
            // Start ViewTasksActivity
            val intent = Intent(this, ViewTasksActivity::class.java)
            startActivity(intent)
        }

        // Set click listener for "Back" button
        backButton.setOnClickListener {
            // Finish current activity and go back
            finish()
        }

        // Load existing tasks
        loadTasks()
    }

    private fun loadTasks() {
        val sharedPreferences = getSharedPreferences("TaskPreferences", Context.MODE_PRIVATE)
        val tasks = sharedPreferences.getStringSet("tasks", mutableSetOf())?.toMutableList() ?: mutableListOf()

        for (task in tasks) {
            addTaskView(task)
        }
    }

    private fun addTaskView(task: String) {
        val taskView: View = LayoutInflater.from(this).inflate(R.layout.item_task_with_timer, tasksContainer, false)

        val taskTextView: TextView = taskView.findViewById(R.id.taskTextView)
        val startButton: Button = taskView.findViewById(R.id.startButton)
        val endButton: Button = taskView.findViewById(R.id.endButton)
        val timerTextView: TextView = taskView.findViewById(R.id.timerTextView)

        taskTextView.text = task
        timerTextView.text = "00:00:00"

        startButton.setOnClickListener {
            startTimer(task, timerTextView)
        }

        endButton.setOnClickListener {
            stopTimer()
        }

        tasksContainer.addView(taskView)
    }

    private fun startTimer(task: String, timerTextView: TextView) {
        if (currentTask != null && currentTask != task) {
            stopTimer() // Stop the previous timer
        }

        isTimerRunning = true
        currentTask = task
        timerValue = taskTimers.getOrDefault(task, 0)
        handler.postDelayed(runnable, 1000)
    }

    private fun stopTimer() {
        isTimerRunning = false
        currentTask?.let {
            taskTimers[it] = timerValue // Save the timer value
            currentTask = null
        }
        timerValue = 0
        handler.removeCallbacks(runnable) // Stop the runnable
    }

    private fun updateTimerView(task: String) {
        for (view in tasksContainer.children) {
            val taskTextView: TextView = view.findViewById(R.id.taskTextView)
            val timerTextView: TextView = view.findViewById(R.id.timerTextView)

            if (taskTextView.text == task) {
                timerTextView.text = formatTime(timerValue)
            }
        }
    }

    private fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }
}