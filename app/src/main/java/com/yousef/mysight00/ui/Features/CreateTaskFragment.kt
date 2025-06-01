package com.yousef.mysight00.ui.Features

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.databinding.FragmentCreateTaskBinding
import com.yousef.mysight00.model.Task
import com.yousef.mysight00.ui.tasks.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

class CreateTaskFragment : Fragment() {

    private var _binding: FragmentCreateTaskBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by viewModels()
    private var editingTask: Task? = null
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get task ID from arguments if in edit mode
        arguments?.getLong("taskId")?.let { taskId ->
            viewModel.getTaskById(taskId) { task ->
                task?.let {
                    editingTask = it
                    loadTaskData(it)
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
            btnSendTaskNow.text = "Update & Send"
        }
    }

    private fun setupViews() {
        // Set current date as default if not editing
        if (editingTask == null) {
            binding.dateCreateTask.setText(dateFormat.format(Date()))
            binding.startTimeCreateTask.setText(timeFormat.format(Date()))
            binding.endTimeCreateTask.setText(timeFormat.format(Date()))
        }
    }

    private fun setupListeners() {
        binding.arrowBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnCreateTask.setOnClickListener {
            if (validateInputs()) {
                if (editingTask != null) {
                    updateTask()
                } else {
                    saveTask()
                }
            }
        }

        binding.btnSendTaskNow.setOnClickListener {
            if (validateInputs()) {
                if (editingTask != null) {
                    updateAndSendTask()
                } else {
                    saveAndSendTask()
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val title = binding.nameCreateTask.text.toString()
        val description = binding.DescriptionCreateTask.text.toString()
        val date = binding.dateCreateTask.text.toString()
        val startTime = binding.startTimeCreateTask.text.toString()
        val endTime = binding.endTimeCreateTask.text.toString()

        if (title.isEmpty()) {
            binding.nameCreateTask.error = "Please enter task title"
            return false
        }

        if (description.isEmpty()) {
            binding.DescriptionCreateTask.error = "Please enter task description"
            return false
        }

        if (date.isEmpty()) {
            binding.dateCreateTask.error = "Please select date"
            return false
        }

        if (startTime.isEmpty()) {
            binding.startTimeCreateTask.error = "Please select start time"
            return false
        }

        if (endTime.isEmpty()) {
            binding.endTimeCreateTask.error = "Please select end time"
            return false
        }

        return true
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

    private fun saveAndSendTask() {
        val task = createTaskFromInputs()
        viewModel.createTask(task)
        viewModel.sendTaskNow(task)
        Toast.makeText(requireContext(), "Task sent successfully", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun updateAndSendTask() {
        val task = createTaskFromInputs().copy(id = editingTask?.id ?: 0)
        viewModel.updateTask(task)
        viewModel.sendTaskNow(task)
        Toast.makeText(requireContext(), "Task updated and sent successfully", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun createTaskFromInputs(): Task {
        val title = binding.nameCreateTask.text.toString()
        val description = binding.DescriptionCreateTask.text.toString()
        val date = binding.dateCreateTask.text.toString()
        val startTime = binding.startTimeCreateTask.text.toString()
        val endTime = binding.endTimeCreateTask.text.toString()

        // TODO: Replace with actual user IDs
        val patientId = "current_patient_id"
        val companionId = "current_companion_id"

        // Parse date and time
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
