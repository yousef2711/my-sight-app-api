package com.yousef.mysight00.ui.Features

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.yousef.mysight00.R
import com.yousef.mysight00.adapter.DayItem
import com.yousef.mysight00.adapter.DaysAdapter
import com.yousef.mysight00.adapter.TaskAdapter
import com.yousef.mysight00.databinding.FragmentTasksBinding
import com.yousef.mysight00.model.Task
import com.yousef.mysight00.ui.base.BaseFragment
import com.yousef.mysight00.ui.tasks.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TasksFragment : BaseFragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter
    private var selectedDate: Date = Date()
    private var allTasks: List<Task> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMonthDisplay()
        setupDaysRecyclerView()
        setupTasksRecyclerView()
        setupObservers()

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

    private fun setupMonthDisplay() {
        val monthFormat = SimpleDateFormat("MMMM, yyyy", Locale.getDefault())
        binding.tvMonth.text = monthFormat.format(Date())
    }

    private fun setupDaysRecyclerView() {
        binding.recyclerViewDays.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        
        val days = generateDays()
        binding.recyclerViewDays.adapter = DaysAdapter(days) { date ->
            selectedDate = date
            filterTasksByDate(date)
        }
    }

    private fun setupTasksRecyclerView() {
        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(requireContext())

        taskAdapter = TaskAdapter(
            onTaskChecked = { task, isChecked ->
                viewModel.updateTaskCompletion(task.id, isChecked)
            },
            onTaskDelete = { task ->
                viewModel.deleteTask(task)
            },
            onTaskEdit = { task ->
                val action = TasksFragmentDirections.actionTasksToCreateTask(task.id)
                findNavController().navigate(action)
            },
            onTaskSend = { task ->
                viewModel.sendTaskNow(task)
            }
        )

        binding.recyclerViewTasks.adapter = taskAdapter
    }

    private fun setupObservers() {
        // TODO: Replace with actual user ID and type
        val userId = "current_user_id"
        val isCompanion = true // or false for patient
        viewModel.getTasksForUser(userId, isCompanion).observe(viewLifecycleOwner) { tasks ->
            allTasks = tasks
            filterTasksByDate(selectedDate)
        }
    }

    private fun filterTasksByDate(date: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.time

        val filteredTasks = allTasks.filter { task ->
            val taskDate = task.startTime
            taskDate in startOfDay..endOfDay
        }

        if (filteredTasks.isEmpty()) {
            binding.tvNoTasks.visibility = View.VISIBLE
            binding.recyclerViewTasks.visibility = View.GONE
            binding.tvNoTasks.text = getString(R.string.no_tasks_for_date, 
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date))
        } else {
            binding.tvNoTasks.visibility = View.GONE
            binding.recyclerViewTasks.visibility = View.VISIBLE
            taskAdapter.submitList(filteredTasks)
        }
    }

    private fun generateDays(): List<DayItem> {
        val calendar = Calendar.getInstance()
        val days = mutableListOf<DayItem>()
        val today = Calendar.getInstance()

        // Go back 3 days
        calendar.add(Calendar.DAY_OF_MONTH, -3)

        // Generate 7 days
        repeat(7) {
            val isToday = calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) &&
                    calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)

            days.add(DayItem(
                date = calendar.time,
                isSelected = isToday
            ))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return days
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
