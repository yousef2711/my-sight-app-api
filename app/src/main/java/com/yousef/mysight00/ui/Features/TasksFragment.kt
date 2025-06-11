package com.yousef.mysight00.ui.Features

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.yousef.mysight00.R
import com.yousef.mysight00.adapter.DayItem
import com.yousef.mysight00.adapter.DaysAdapter
import com.yousef.mysight00.adapter.TaskAdapter
import com.yousef.mysight00.databinding.FragmentTasksBinding
import com.yousef.mysight00.model.Task
import com.yousef.mysight00.ui.tasks.TaskViewModel
import com.yousef.mysight00.utils.UserPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var daysAdapter: DaysAdapter
    private lateinit var viewModel: TaskViewModel
    private lateinit var userPreferences: UserPreferences
    private var selectedDate: Date = Calendar.getInstance().time // Initialize with current date

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())
        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDaysRecyclerView()
        setupTasksRecyclerView()
        setupListeners()
        updateMonthAndYear()
        loadTasks() // Load tasks for the initial selectedDate (current date)
    }

    private fun setupDaysRecyclerView() {
        binding.recyclerViewDays.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        daysAdapter = DaysAdapter(generateDays()) { date ->
            // Handle day click
            selectedDate = date
            loadTasks() // Reload tasks for the newly selected date
        }
        binding.recyclerViewDays.adapter = daysAdapter
    }

    private fun setupTasksRecyclerView() {
        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(requireContext())
        taskAdapter = TaskAdapter(
            onTaskChecked = { task, isChecked ->
                viewModel.updateTaskCompletion(task.id, isChecked)
            },
            onTaskDelete = { task ->
                viewModel.deleteTask(task)
                loadTasks() // Reload tasks after deletion
            },
            onTaskEdit = { task ->
                val action = TasksFragmentDirections.actionTasksToCreateTask(task.id)
                findNavController().navigate(action)
            }
        )
        binding.recyclerViewTasks.adapter = taskAdapter
    }

    private fun setupListeners() {
        binding.apply {
            arrowBack.setOnClickListener {
                findNavController().navigate(R.id.action_tasks_to_home)
            }

            btnNotifications.setOnClickListener {
                findNavController().navigate(R.id.action_tasks_to_notification)
            }

            btnAddTask.setOnClickListener {
                findNavController().navigate(R.id.action_tasks_to_create_task)
            }
        }
    }

    private fun generateDays(): List<DayItem> {
        val calendar = Calendar.getInstance()
        // Set calendar to 3 days before today
        calendar.time = Date() // Reset to current date
        calendar.add(Calendar.DAY_OF_MONTH, -3)

        val days = mutableListOf<DayItem>()

        repeat(7) { // Generate 7 days (3 before, current, 3 after)
            val dayItem = DayItem(calendar.time)
            days.add(dayItem)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }

    private fun updateMonthAndYear() {
        val currentMonthAndYear = SimpleDateFormat("MMM, yyyy", Locale.getDefault()).format(Date())
        binding.tvMonth.text = currentMonthAndYear
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
            val filteredTasks = allTasks.filter { task ->
                isTaskRelevantForSelectedDay(task, selectedDate)
            }
            updateTasksList(filteredTasks)
        }
    }

    private fun isTaskRelevantForSelectedDay(task: Task, selectedDate: Date): Boolean {
        val calendar = Calendar.getInstance()

        // Normalize selectedDate to start of day
        calendar.time = selectedDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDaySelected = calendar.time

        // Normalize selectedDate to end of day
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDaySelected = calendar.time

        // Check if task starts within the selected day
        val taskStartsInSelectedDay = task.startTime?.let { startTime ->
            startTime.after(startOfDaySelected) && startTime.before(endOfDaySelected)
        } ?: false

        // Check if the selected day is within the task's duration
        val selectedDayWithinTaskDuration = task.startTime != null && task.endTime != null &&
                selectedDate.after(task.startTime) && selectedDate.before(task.endTime)

        return taskStartsInSelectedDay || selectedDayWithinTaskDuration
    }

    private fun updateTasksList(tasks: List<Task>) {
        if (tasks.isEmpty()) {
            binding.tvNoTasks.visibility = View.VISIBLE
            binding.recyclerViewTasks.visibility = View.GONE
            // Optionally, update the text to show the selected date for clarity
            binding.tvNoTasks.text = getString(R.string.no_tasks_for_date, SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate))
        } else {
            binding.tvNoTasks.visibility = View.GONE
            binding.recyclerViewTasks.visibility = View.VISIBLE
            taskAdapter.submitList(tasks)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



