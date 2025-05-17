package com.yousef.mysight00.ui

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.R
import com.yousef.mysight00.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private var selectedUserType: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupValidation()
        setupUserTypeSelection()
        setupNavigation()
    }

    private fun setupValidation() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = validateFields()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.apply {
            nameRegisComp.addTextChangedListener(watcher)
            emailRegisComp.addTextChangedListener(watcher)
            passwordRegisComp.addTextChangedListener(watcher)
            ageRegisComp.addTextChangedListener(watcher)
            phNumRegisComp.addTextChangedListener(watcher)
        }
    }

    private fun validateFields() {
        val name = binding.nameRegisComp.text.toString().trim()
        val email = binding.emailRegisComp.text.toString().trim()
        val password = binding.passwordRegisComp.text.toString().trim()
        val age = binding.ageRegisComp.text.toString().trim()
        val phone = binding.phNumRegisComp.text.toString().trim()

        val validName = name.isNotEmpty() && name.length >= 3
        val validEmail = email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val validPassword = password.isNotEmpty() && password.length >= 6
        val validAge = age.isNotEmpty() && age.toIntOrNull()?.let { it > 0 } == true
        val validPhone = phone.isNotEmpty() && phone.length >= 10 && phone.all { it.isDigit() }

        val isFormValid = validName && validEmail && validPassword && validAge && validPhone && selectedUserType != null

        Log.d("RegisterFragment", "Form validation: validName=$validName, validEmail=$validEmail, " +
                "validPassword=$validPassword, validAge=$validAge, validPhone=$validPhone, " +
                "selectedUserType=$selectedUserType, isFormValid=$isFormValid")

        binding.btnRegisComp.isEnabled = isFormValid

        binding.apply {
            nameRegisComp.error = if (!validName) "Please enter a valid name" else null
            emailRegisComp.error = if (!validEmail) "Please enter a valid email" else null
            passwordRegisComp.error = if (!validPassword) "Password must be at least 6 characters" else null
            ageRegisComp.error = if (!validAge) "Please enter a valid age" else null
            phNumRegisComp.error = if (!validPhone) "Please enter a valid phone number" else null
        }
    }

    private fun setupUserTypeSelection() {
        resetUserTypeButtons()

        binding.apply {
            btnblindRegisComp.setOnClickListener { if (selectedUserType != "Blind") selectUserType("Blind") }
            btnalzheimerRegisComp.setOnClickListener { if (selectedUserType != "Alzheimer") selectUserType("Alzheimer") }
            btncompanionRegisComp.setOnClickListener { if (selectedUserType != "Companion") selectUserType("Companion") }
        }
    }

    private fun selectUserType(type: String) {
        selectedUserType = type
        resetUserTypeButtons()

        when (type) {
            "Blind" -> {
                binding.btnblindRegisComp.setChipBackgroundColorResource(R.color.primary_blue)
                binding.btnblindRegisComp.setTextColor(Color.WHITE)
            }
            "Alzheimer" -> {
                binding.btnalzheimerRegisComp.setChipBackgroundColorResource(R.color.primary_blue)
                binding.btnalzheimerRegisComp.setTextColor(Color.WHITE)
            }
            "Companion" -> {
                binding.btncompanionRegisComp.setChipBackgroundColorResource(R.color.primary_blue)
                binding.btncompanionRegisComp.setTextColor(Color.WHITE)
            }
        }

        validateFields()
    }

    private fun resetUserTypeButtons() {
        binding.apply {
            btnblindRegisComp.setChipBackgroundColorResource(R.color.gray_lite)
            btnblindRegisComp.setTextColor(Color.BLACK)

            btnalzheimerRegisComp.setChipBackgroundColorResource(R.color.gray_lite)
            btnalzheimerRegisComp.setTextColor(Color.BLACK)

            btncompanionRegisComp.setChipBackgroundColorResource(R.color.gray_lite)
            btncompanionRegisComp.setTextColor(Color.BLACK)
        }
    }

    private fun setupNavigation() {
        binding.btnRegisComp.setOnClickListener {
            Log.d("RegisterFragment", "Register button clicked")
            Log.d("RegisterFragment", "Selected user type: $selectedUserType")

            if (binding.btnRegisComp.isEnabled) {
                when (selectedUserType) {
                    "Companion" -> {
                        Log.d("RegisterFragment", "Navigating to form")
                        findNavController().navigate(R.id.action_register_to_form)
                    }
                    else -> {
                        Log.d("RegisterFragment", "Navigating to login")
                        findNavController().navigate(R.id.action_register_to_login)
                    }
                }
            } else {
                Log.d("RegisterFragment", "Button is disabled, form is not valid")
            }
        }

        binding.arrowBackRegisComp.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }

        binding.btnLoginRegisComp.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }

        binding.logTextRegisComp.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
