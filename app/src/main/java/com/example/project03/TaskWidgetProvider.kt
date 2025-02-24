package com.example.project03

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class TaskWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val sharedPreferences = context.getSharedPreferences("TaskPreferences", Context.MODE_PRIVATE)
        val widgetTasks = sharedPreferences.getStringSet("tasks", emptySet())?.toList() ?: emptyList()

        // Create an intent to launch the HomeActivity when the widget is clicked
        val intent = Intent(context, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Get the layout for the widget and attach an on-click listener to the root view
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)

        // Update the task list in the widget
        if (widgetTasks.isNotEmpty()) {
            val taskText = widgetTasks.joinToString(separator = "\n") { it }
            views.setTextViewText(R.id.widgetTaskList, taskText)
        } else {
            views.setTextViewText(R.id.widgetTaskList, "No tasks available")
        }

        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}