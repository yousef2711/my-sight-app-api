        package com.yousef.mysight00.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
    private val isHistoryMode: Boolean = false
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    private val selectedTasks = mutableSetOf<Task>()
    private var isSelectionMode = false

    fun toggleSelectionMode() {
        isSelectionMode = !isSelectionMode
        if (!isSelectionMode) {
            selectedTasks.clear()
        }
        notifyDataSetChanged()
    }

    fun getSelectedTasks(): Set<Task> = selectedTasks.toSet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task, selectedTasks.contains(task))
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

        fun bind(task: Task, isSelected: Boolean) {
            binding.apply {
                textViewTitle.text = task.title
                textViewDescription.text = task.description
                checkBoxTask.isChecked = task.isCompleted
                
                val timeText = "Start: ${dateFormat.format(task.startTime)}\n" +
                             "End: ${dateFormat.format(task.endTime)}"
                textViewScheduledTime.text = timeText
                textViewScheduledTime.visibility = android.view.View.VISIBLE

                // Hide edit button in history mode
                buttonEdit.visibility = if (isHistoryMode) android.view.View.GONE else android.view.View.VISIBLE

                // Handle selection mode
                if (isSelectionMode) {
                    buttonDelete.setColorFilter(
                        ContextCompat.getColor(
                            root.context,
                            if (isSelected) R.color.red else R.color.dark
                        )
                    )
                    root.setOnClickListener {
                        if (isSelected) {
                            selectedTasks.remove(task)
                        } else {
                            selectedTasks.add(task)
                        }
                        notifyItemChanged(adapterPosition)
                    }
                } else {
                    buttonDelete.setColorFilter(
                        ContextCompat.getColor(root.context, R.color.dark)
                    )
                    root.setOnClickListener(null)
                    buttonDelete.setOnClickListener {
                        onTaskDelete(task)
                    }
                }

                checkBoxTask.setOnCheckedChangeListener { _, isChecked ->
                    onTaskChecked(task, isChecked)
                }

                buttonEdit.setOnClickListener {
                    onTaskEdit(task)
                }
            }
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
