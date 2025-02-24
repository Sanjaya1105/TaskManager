package com.example.project03

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.*

class CreateTaskActivity : AppCompatActivity() {

    private lateinit var taskNameEditText: EditText
    private lateinit var taskDateEditText: EditText
    private lateinit var taskTimeEditText: EditText
    private lateinit var createTaskButton: Button
    private lateinit var cancelButton: Button

    private var calendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        // Initialize views
        taskNameEditText = findViewById(R.id.taskName)
        taskDateEditText = findViewById(R.id.taskDate)
        taskTimeEditText = findViewById(R.id.taskTime)
        createTaskButton = findViewById(R.id.createTaskButton)
        cancelButton = findViewById(R.id.cancelButton)

        // Show date picker on click
        taskDateEditText.setOnClickListener { showDatePicker() }

        // Show time picker on click
        taskTimeEditText.setOnClickListener { showTimePicker() }

        // Handle create button click
        createTaskButton.setOnClickListener {
            if (checkNotificationPermission()) {
                createTask()
            }
        }

        // Handle cancel button click
        cancelButton.setOnClickListener { finish() } // Return to home page
    }

    // Show Date Picker Dialog
    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                taskDateEditText.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    // Show Time Picker Dialog
    private fun showTimePicker() {
        val timePicker = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                taskTimeEditText.setText(timeFormat.format(calendar.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }

    // Handle task creation
    private fun createTask() {
        val taskName = taskNameEditText.text.toString().trim()
        val taskDate = taskDateEditText.text.toString().trim()
        val taskTime = taskTimeEditText.text.toString().trim()

        if (taskName.isNotEmpty() && taskDate.isNotEmpty() && taskTime.isNotEmpty()) {
            // Format task data as a single string
            val task = "$taskName - $taskDate @ $taskTime"

            // Retrieve and update the task set in SharedPreferences
            val sharedPreferences = getSharedPreferences("TaskPreferences", Context.MODE_PRIVATE)
            val tasks = sharedPreferences.getStringSet("tasks", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

            // Check for duplicates before adding
            if (tasks.contains(task)) {
                Toast.makeText(this, "Task already exists!", Toast.LENGTH_SHORT).show()
            } else {
                tasks.add(task)

                // Save the updated set back to SharedPreferences
                sharedPreferences.edit {
                    putStringSet("tasks", tasks)
                    apply()
                }

                // Schedule notification
                scheduleNotification(taskName, tasks.size)

                Toast.makeText(this, "Task created!", Toast.LENGTH_SHORT).show()

                // Return to home page
                finish()
            }
        } else {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
        }
    }

    // Schedule daily notification with a unique request code based on task count
    private fun scheduleNotification(taskName: String, requestCode: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("taskName", taskName)
        }

        // Use unique requestCode to avoid PendingIntent conflicts
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the alarm to start at the selected time every day
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    // Check and request notification permission for Android 13+
    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                false
            }
        } else {
            true // Permission not required for lower versions
        }
    }

    // Handle permission request result
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                createTask() // Proceed with creating task if permission is granted
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
}