package com.yousef.mysight00.ui.Features

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.yousef.mysight00.R
import com.yousef.mysight00.adapter.TaskAdapter
import com.yousef.mysight00.databinding.FragmentHistoryBinding
import com.yousef.mysight00.model.Task
import com.yousef.mysight00.ui.tasks.TaskViewModel
import com.yousef.mysight00.utils.UserPreferences
import java.util.Calendar
import java.util.Date

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var viewModel: TaskViewModel
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())
        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        loadTasks()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewTasksHistory.layoutManager = LinearLayoutManager(requireContext())
        taskAdapter = TaskAdapter(
            onTaskChecked = { task, isChecked ->
                // Disable task completion in history
            },
            onTaskDelete = { task ->
                // Task selection is handled in the adapter
            },
            onTaskEdit = { task ->
                // Disable editing in history
            },
            isHistoryMode = true
        )
        binding.recyclerViewTasksHistory.adapter = taskAdapter
    }

    private fun setupListeners() {
        binding.apply {
            backButtonHistory.setOnClickListener {
                findNavController().navigate(R.id.action_history_to_home)
            }

            tvClearHistory.setOnClickListener {
                if (taskAdapter.getSelectedTasks().isEmpty()) {
                    // Enter selection mode
                    taskAdapter.toggleSelectionMode()
                    tvClearHistory.text = "Delete Selected"
                    Toast.makeText(requireContext(), "Select tasks to delete", Toast.LENGTH_SHORT).show()
                } else {
                    // Delete selected tasks
                    taskAdapter.getSelectedTasks().forEach { task ->
                        viewModel.deleteTask(task)
                    }
                    
                    Toast.makeText(requireContext(), "Selected tasks cleared", Toast.LENGTH_SHORT).show()
                    taskAdapter.toggleSelectionMode()
                    tvClearHistory.text = "Clear"
                    loadTasks() // Reload tasks after deletion
                }
            }
        }
    }

    private fun loadTasks() {
        val userType = userPreferences.getUserType() ?: ""
        val userId = userPreferences.getUserId() ?: ""

        val tasksLiveData = if (userType == "companions") {
            viewModel.getCompanionTasks(userId)
        } else {
            viewModel.getPatientTasks(userId)
        }

        tasksLiveData.observe(viewLifecycleOwner) { allTasks ->
            // Sort tasks by date (current and past tasks first, then future tasks)
            val sortedTasks = allTasks.sortedWith(compareBy { task ->
                val now = Calendar.getInstance().time
                if (task.startTime.before(now)) 0 else 1
            })
            taskAdapter.submitList(sortedTasks)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
