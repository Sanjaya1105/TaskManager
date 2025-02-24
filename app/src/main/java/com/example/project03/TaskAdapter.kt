package com.example.project03

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView

class TaskAdapter(context: Context, private val tasks: MutableList<String>, private val listener: TaskActionListener) :
    ArrayAdapter<String>(context, 0, tasks) {

    interface TaskActionListener {
        fun onDelete(task: String)
        fun onUpdate(task: String)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)

        val taskTextView = view.findViewById<TextView>(R.id.taskTextView)
        val deleteButton = view.findViewById<Button>(R.id.deleteButton)
        val updateButton = view.findViewById<Button>(R.id.updateButton)

        val task = tasks[position]

        taskTextView.text = task

        deleteButton.setOnClickListener {
            listener.onDelete(task)
        }

        updateButton.setOnClickListener {
            listener.onUpdate(task)
        }

        return view
    }
}