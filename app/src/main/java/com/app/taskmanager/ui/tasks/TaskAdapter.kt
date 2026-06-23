package com.app.taskmanager.ui.tasks

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.taskmanager.R
import com.app.taskmanager.data.local.entity.Priority
import com.app.taskmanager.data.local.entity.Task
import com.app.taskmanager.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onTaskChecked: (Task, Boolean) -> Unit,
    private val onTaskClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.apply {
                checkboxTask.isChecked = task.isCompleted
                textTitle.text = task.title
                textDescription.text = task.description.ifBlank { "No description" }

                // Strikethrough if completed
                if (task.isCompleted) {
                    textTitle.paintFlags = textTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    textTitle.alpha = 0.5f
                } else {
                    textTitle.paintFlags = textTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    textTitle.alpha = 1.0f
                }

                // Priority indicator
                val priorityColor = when (task.priority) {
                    Priority.HIGH -> R.color.priority_high
                    Priority.MEDIUM -> R.color.priority_medium
                    Priority.LOW -> R.color.priority_low
                }
                viewPriorityIndicator.setBackgroundColor(
                    ContextCompat.getColor(root.context, priorityColor)
                )
                textPriority.text = task.priority.name

                // Due date
                task.dueDate?.let { timestamp ->
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    textDueDate.text = sdf.format(Date(timestamp))
                    textDueDate.visibility = android.view.View.VISIBLE
                } ?: run {
                    textDueDate.visibility = android.view.View.GONE
                }

                // Listeners
                checkboxTask.setOnCheckedChangeListener { _, isChecked ->
                    onTaskChecked(task, isChecked)
                }
                root.setOnClickListener { onTaskClick(task) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }
}
