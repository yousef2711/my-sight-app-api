package com.yousef.mysight00.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.yousef.mysight00.R
import com.yousef.mysight00.databinding.FragmentTasksBinding
import com.yousef.mysight00.adapter.DaysAdapter
import com.yousef.mysight00.adapter.TaskAdapter
import com.yousef.mysight00.model.TaskModel
import java.text.SimpleDateFormat
import java.util.*

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter

    private val taskList = mutableListOf(
        TaskModel("Design Changes", "2 Days ago", false),
        TaskModel("Fix Bugs", "3 Days ago", true),
        TaskModel("Update UI", "1 Day ago", false)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDaysRecyclerView()
        setupTasksRecyclerView()

        binding.btnNotifications.setOnClickListener {
            findNavController().navigate(R.id.action_tasks_to_notification)
        }

        binding.btnAddTask.setOnClickListener {
            findNavController().navigate(R.id.action_tasks_to_create_task)
        }

        binding.arrowBack.setOnClickListener {
            findNavController().navigate(R.id.action_tasks_to_home)
        }
    }

    private fun setupDaysRecyclerView() {
        binding.recyclerViewDays.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewDays.adapter = DaysAdapter(generateDays()) { day ->
            println("Day clicked: $day")
        }
    }

    private fun setupTasksRecyclerView() {
        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(requireContext())

        taskAdapter = TaskAdapter(taskList, object : TaskAdapter.TaskActionListener {
            override fun onSendTask(task: TaskModel) {
                println("Sending task: ${task.name}")
            }

            override fun onDeleteTask(task: TaskModel) {
                taskList.remove(task)
                taskAdapter.notifyDataSetChanged()
            }

            override fun onEditTask(task: TaskModel) {
                println("Editing task: ${task.name}")
            }
        })

        binding.recyclerViewTasks.adapter = taskAdapter
    }

    private fun generateDays(): List<String> {
        val calendar = Calendar.getInstance()
        val days = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("d", Locale.getDefault())

        repeat(7) {
            days.add(dateFormat.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return days
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
