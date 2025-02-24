package com.example.project03

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.*

class ViewTasksActivity : AppCompatActivity(), TaskAdapter.TaskActionListener {

    private lateinit var taskListView: ListView
    private lateinit var tasks: MutableSet<String>
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_tasks)

        taskListView = findViewById(R.id.taskListView)

        // Load tasks from SharedPreferences
        val sharedPreferences = getSharedPreferences("TaskPreferences", Context.MODE_PRIVATE)
        tasks = sharedPreferences.getStringSet("tasks", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        // Set up the adapter
        taskAdapter = TaskAdapter(this, tasks.toMutableList(), this)
        taskListView.adapter = taskAdapter
    }

    override fun onDelete(task: String) {
        tasks.remove(task)
        updateTasksInPreferences()
    }

    override fun onUpdate(task: String) {
        showUpdateDialog(task)
    }

    private fun showUpdateDialog(oldTask: String) {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_update_task, null)

        val taskNameEditText = view.findViewById<EditText>(R.id.taskNameEditText)
        val taskDateEditText = view.findViewById<EditText>(R.id.taskDateEditText)
        val taskTimeEditText = view.findViewById<EditText>(R.id.taskTimeEditText)

        // Split the old task string to populate the EditTexts
        val taskParts = oldTask.split(" - ", " @ ")
        if (taskParts.size == 3) {
            taskNameEditText.setText(taskParts[0])
            taskDateEditText.setText(taskParts[1])
            taskTimeEditText.setText(taskParts[2])
        }

        // Set up the calendar and time pickers
        val calendar = Calendar.getInstance()

        // DatePickerDialog
        taskDateEditText.setOnClickListener {
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                taskDateEditText.setText(dateFormat.format(calendar.time))
            }
            DatePickerDialog(
                this, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // TimePickerDialog
        taskTimeEditText.setOnClickListener {
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                taskTimeEditText.setText(timeFormat.format(calendar.time))
            }
            TimePickerDialog(
                this, timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Update Task")
            .setView(view)
            .setPositiveButton("Update") { _, _ ->
                val newTaskName = taskNameEditText.text.toString().trim()
                val newTaskDate = taskDateEditText.text.toString().trim()
                val newTaskTime = taskTimeEditText.text.toString().trim()

                if (newTaskName.isNotEmpty() && newTaskDate.isNotEmpty() && newTaskTime.isNotEmpty()) {
                    // Construct the new task string
                    val newTask = "$newTaskName - $newTaskDate @ $newTaskTime"
                    tasks.remove(oldTask) // Remove old task
                    tasks.add(newTask) // Add updated task
                    updateTasksInPreferences()
                } else {
                    Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun updateTasksInPreferences() {
        val sharedPreferences = getSharedPreferences("TaskPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putStringSet("tasks", tasks)
            apply()
        }
        taskAdapter.clear()
        taskAdapter.addAll(tasks.toList())
    }
}
