package com.yousef.mysight00.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yousef.mysight00.R
import com.yousef.mysight00.databinding.ItemTaskBinding
import com.yousef.mysight00.model.Task
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapter(
    private val onTaskChecked: (Task, Boolean) -> Unit,
    private val onTaskDelete: (Task) -> Unit,
    private val onTaskEdit: (Task) -> Unit,
    private val onTaskSend: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

        fun bind(task: Task) {
            binding.apply {
                textViewTitle.text = task.title
                textViewDescription.text = task.description
                checkBoxTask.isChecked = task.isCompleted
                
                val timeText = "Start: ${dateFormat.format(task.startTime)}\n" +
                             "End: ${dateFormat.format(task.endTime)}"
                textViewScheduledTime.text = timeText
                textViewScheduledTime.visibility = android.view.View.VISIBLE

                checkBoxTask.setOnCheckedChangeListener { _, isChecked ->
                    onTaskChecked(task, isChecked)
                }

                buttonDelete.setOnClickListener {
                    showTaskOptions(task)
                }
            }
        }

        private fun showTaskOptions(task: Task) {
            val popup = PopupMenu(binding.root.context, binding.buttonDelete)
            popup.menuInflater.inflate(R.menu.menu_task_options, popup.menu)
            
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_send -> {
                        onTaskSend(task)
                        true
                    }
                    R.id.action_edit -> {
                        onTaskEdit(task)
                        true
                    }
                    R.id.action_delete -> {
                        onTaskDelete(task)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
