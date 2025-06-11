package com.yousef.mysight00.ui.Features

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.databinding.FragmentCreateTaskBinding
import com.yousef.mysight00.model.Task
import com.yousef.mysight00.ui.base.BaseFragment
import com.yousef.mysight00.ui.tasks.TaskViewModel
import com.yousef.mysight00.utils.UserPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateTaskFragment : BaseFragment() {

    private var _binding: FragmentCreateTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TaskViewModel
    private var editingTask: Task? = null
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateTaskBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())
        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get task ID from arguments if in edit mode
        arguments?.getLong("taskId")?.let { taskId ->
            if (taskId != -1L) {
                viewModel.getTaskById(taskId) { task ->
                    task?.let {
                        editingTask = it
                        loadTaskData(it)
                    }
                }
            }
        }

        setupViews()
        setupListeners()
    }

    private fun loadTaskData(task: Task) {
        binding.apply {
            nameCreateTask.setText(task.title)
            DescriptionCreateTask.setText(task.description)
            dateCreateTask.setText(dateFormat.format(task.startTime))
            startTimeCreateTask.setText(timeFormat.format(task.startTime))
            endTimeCreateTask.setText(timeFormat.format(task.endTime))
            btnCreateTask.text = "Update Task"
        }
    }

    private fun setupViews() {
        // Set current date and time as default if not editing
        if (editingTask == null) {
            binding.apply {
                dateCreateTask.setText(dateFormat.format(Date()))
                startTimeCreateTask.setText(timeFormat.format(Date()))
                endTimeCreateTask.setText(timeFormat.format(Date()))
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            arrowBack.setOnClickListener {
                findNavController().popBackStack()
            }

            btnCreateTask.setOnClickListener {
                if (validateInputs()) {
                    if (editingTask != null) {
                        updateTask()
                    } else {
                        saveTask()
                    }
                }
            }

            // إضافة مستمعي التاريخ والوقت
            dateCreateTask.setOnClickListener {
                showDatePicker()
            }

            startTimeCreateTask.setOnClickListener {
                showTimePicker(true)
            }

            endTimeCreateTask.setOnClickListener {
                showTimePicker(false)
            }
        }
    }

    private fun validateInputs(): Boolean {
        binding.apply {
            val title = nameCreateTask.text.toString()
            val description = DescriptionCreateTask.text.toString()
            val date = dateCreateTask.text.toString()
            val startTime = startTimeCreateTask.text.toString()
            val endTime = endTimeCreateTask.text.toString()

            if (title.isEmpty()) {
                nameCreateTask.error = "Please enter task title"
                return false
            }

            if (description.isEmpty()) {
                DescriptionCreateTask.error = "Please enter task description"
                return false
            }

            if (date.isEmpty()) {
                dateCreateTask.error = "Please select date"
                return false
            }

            if (startTime.isEmpty()) {
                startTimeCreateTask.error = "Please select start time"
                return false
            }

            if (endTime.isEmpty()) {
                endTimeCreateTask.error = "Please select end time"
                return false
            }

            // التحقق من أن وقت الانتهاء بعد وقت البدء
            val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val startDateTime = dateTimeFormat.parse("$date $startTime")
            val endDateTime = dateTimeFormat.parse("$date $endTime")

            if (startDateTime != null && endDateTime != null && endDateTime.before(startDateTime)) {
                endTimeCreateTask.error = "End time must be after start time"
                return false
            }

            return true
        }
    }

    private fun saveTask() {
        val task = createTaskFromInputs()
        viewModel.createTask(task)
        Toast.makeText(requireContext(), "Task created successfully", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun updateTask() {
        val task = createTaskFromInputs().copy(id = editingTask?.id ?: 0)
        viewModel.updateTask(task)
        Toast.makeText(requireContext(), "Task updated successfully", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun createTaskFromInputs(): Task {
        binding.apply {
            val title = nameCreateTask.text.toString()
            val description = DescriptionCreateTask.text.toString()
            val date = dateCreateTask.text.toString()
            val startTime = startTimeCreateTask.text.toString()
            val endTime = endTimeCreateTask.text.toString()

            // الحصول على نوع المستخدم ومعرفه
            val userType = userPreferences.getUserType() ?: ""
            val userId = userPreferences.getUserId() ?: ""
            
            // تحديد معرفات المريض والمرافق
            val (patientId, companionId) = if (userType == "companions") {
                // إذا كان المستخدم مرافق، نستخدم معرف المريض المسؤول عنه
                Pair(userPreferences.getPatientId() ?: "", userId)
            } else {
                // إذا كان المستخدم مريض، نستخدم معرف المرافق المسؤول عنه
                Pair(userId, userPreferences.getCompanionId() ?: "")
            }

            // تحويل التاريخ والوقت
            val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val startDateTime = dateTimeFormat.parse("$date $startTime")
            val endDateTime = dateTimeFormat.parse("$date $endTime")

            return Task(
                title = title,
                description = description,
                patientId = patientId,
                companionId = companionId,
                startTime = startDateTime ?: Date(),
                endTime = endDateTime ?: Date()
            )
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                binding.dateCreateTask.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = android.app.TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                if (isStartTime) {
                    binding.startTimeCreateTask.setText(timeFormat.format(calendar.time))
                } else {
                    binding.endTimeCreateTask.setText(timeFormat.format(calendar.time))
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

