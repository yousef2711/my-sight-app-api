package com.yousef.mysight00.ui.Auth

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.R
import com.yousef.mysight00.RetrofitInstance
import com.yousef.mysight00.databinding.FragmentRegisterBinding
import com.yousef.mysight00.model.RegisterRequest
import com.yousef.mysight00.model.UserType
import com.yousef.mysight00.ui.base.BaseFragment
import com.yousef.mysight00.utils.showToast
import kotlinx.coroutines.launch

class RegisterFragment : BaseFragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private var selectedUserType: UserType? = null

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
            override fun afterTextChanged(s: Editable?) {
                validateFields(false)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.apply {
            nameRegisComp.addTextChangedListener(watcher)
            emailRegisComp.addTextChangedListener(watcher)
            passwordRegisComp.addTextChangedListener(watcher)
            ageRegisComp.addTextChangedListener(watcher)
            phNumRegisComp.addTextChangedListener(watcher)
            namePatient.addTextChangedListener(watcher)
            relationPatient.addTextChangedListener(watcher)
        }
    }

    private fun validateFields(showErrors: Boolean = false): Boolean {
        val name = binding.nameRegisComp.text.toString().trim()
        val email = binding.emailRegisComp.text.toString().trim()
        val password = binding.passwordRegisComp.text.toString().trim()
        val age = binding.ageRegisComp.text.toString().trim()
        val phone = binding.phNumRegisComp.text.toString().trim()
        val patientName = binding.namePatient.text.toString().trim()
        val patientRelation = binding.relationPatient.text.toString().trim()

        val validName = name.isNotEmpty() && name.length >= 3
        val validEmail = email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val validPassword = password.isNotEmpty() && isPasswordStrong(password)
        val validAge = age.isNotEmpty() && age.toIntOrNull()?.let { it in 1..120 } == true
        val validPhone = phone.isNotEmpty() && isValidPhoneNumber(phone)
        val validTypeSelected = selectedUserType != null
        val isCompanion = selectedUserType == UserType.COMPANION
        val validPatientName = if (isCompanion) patientName.isNotEmpty() else true
        val validRelation = if (isCompanion) patientRelation.isNotEmpty() else true

        val isFormValid = validName && validEmail && validPassword && validAge && validPhone &&
                validTypeSelected && validPatientName && validRelation

        if (showErrors) {
            binding.apply {
                nameRegisComp.error = if (!validName) "Name must be at least 3 characters" else null
                emailRegisComp.error = if (!validEmail) "Please enter a valid email" else null
                passwordRegisComp.error = if (!validPassword) "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number and one special character" else null
                ageRegisComp.error = if (!validAge) "Please enter a valid age (1-120)" else null
                phNumRegisComp.error = if (!validPhone) "Please enter a valid phone number" else null
                if (!validTypeSelected) {
                    requireContext().showToast("Please select user type")
                }
                if (isCompanion) {
                    namePatient.error = if (!validPatientName) "Please enter patient name" else null
                    relationPatient.error = if (!validRelation) "Please enter relationship" else null
                }
            }

            val firstInvalidField = when {
                !validName -> binding.nameRegisComp
                !validEmail -> binding.emailRegisComp
                !validPassword -> binding.passwordRegisComp
                !validAge -> binding.ageRegisComp
                !validPhone -> binding.phNumRegisComp
                !validTypeSelected -> null
                isCompanion && !validPatientName -> binding.namePatient
                isCompanion && !validRelation -> binding.relationPatient
                else -> null
            }
            firstInvalidField?.requestFocus()
            firstInvalidField?.let { view ->
                binding.scrollViewRegis.post {
                    binding.scrollViewRegis.smoothScrollTo(0, view.top)
                }
            }
        }

        binding.btnRegisComp.isEnabled = isFormValid
        return isFormValid
    }

    private fun isPasswordStrong(password: String): Boolean {
        val passwordPattern =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
        return password.matches(passwordPattern.toRegex())
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        val phonePattern = "^[+]?[0-9]{10,15}$"
        return phone.matches(phonePattern.toRegex())
    }

    private fun setupUserTypeSelection() {
        resetUserTypeButtons()

        binding.apply {
            btnblindRegisComp.setOnClickListener {
                if (selectedUserType != UserType.BLIND) selectUserType(UserType.BLIND)
            }
            btnalzheimerRegisComp.setOnClickListener {
                if (selectedUserType != UserType.ALZHEIMER) selectUserType(UserType.ALZHEIMER)
            }
            btncompanionRegisComp.setOnClickListener {
                if (selectedUserType != UserType.COMPANION) selectUserType(UserType.COMPANION)
            }
        }
    }

    private fun selectUserType(type: UserType) {
        selectedUserType = type
        resetUserTypeButtons()

        when (type) {
            UserType.BLIND -> {
                binding.btnblindRegisComp.setChipBackgroundColorResource(R.color.primary_blue)
                binding.btnblindRegisComp.setTextColor(Color.WHITE)
                binding.relationPatient.visibility = View.GONE
                binding.namePatient.visibility = View.GONE
            }

            UserType.ALZHEIMER -> {
                binding.btnalzheimerRegisComp.setChipBackgroundColorResource(R.color.primary_blue)
                binding.btnalzheimerRegisComp.setTextColor(Color.WHITE)
                binding.relationPatient.visibility = View.GONE
                binding.namePatient.visibility = View.GONE
            }

            UserType.COMPANION -> {
                binding.btncompanionRegisComp.setChipBackgroundColorResource(R.color.primary_blue)
                binding.btncompanionRegisComp.setTextColor(Color.WHITE)
                binding.relationPatient.visibility = View.VISIBLE
                binding.namePatient.visibility = View.VISIBLE
                validateFields(true)
            }
        }

        validateFields(false)
    }

    private fun resetUserTypeButtons() {
        val buttons = listOf(
            binding.btnblindRegisComp,
            binding.btnalzheimerRegisComp,
            binding.btncompanionRegisComp
        )
        buttons.forEach {
            it.setChipBackgroundColorResource(R.color.gray_lite)
            it.setTextColor(Color.BLACK)
        }
    }

    private fun setupNavigation() {
        binding.btnRegisComp.setOnClickListener {
            if (!validateFields(true)) return@setOnClickListener

            val registerRequest = RegisterRequest(
                username = binding.nameRegisComp.text.toString().trim(),
                email = binding.emailRegisComp.text.toString().trim(),
                password = binding.passwordRegisComp.text.toString().trim(),
                phone_number = binding.phNumRegisComp.text.toString().trim(),
                name = selectedUserType?.nameValue ?: "",
                account_type = if (selectedUserType == UserType.BLIND || selectedUserType == UserType.ALZHEIMER) "patients" else "companions",
                patient_username = if (selectedUserType == UserType.COMPANION) binding.namePatient.text.toString().trim() else null,
                relationship = if (selectedUserType == UserType.COMPANION) binding.relationPatient.text.toString().trim() else null
            )

            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.getApi(requireContext()).registerUser(registerRequest)
                    if (response.isSuccessful) {
                        Log.i("RegisterFragment", "User registered successfully")
                        requireContext().showToast("Registration successful")
                        findNavController().navigate(R.id.action_register_to_login)
                    } else {
                        val error = response.errorBody()?.string()
                        Log.e("RegisterFragment", "Registration failed: $error")
                        requireContext().showToast("Registration failed: $error")
                    }
                } catch (e: Exception) {
                    Log.e("RegisterFragment", "Error during registration", e)
                    requireContext().showToast("An error occurred: ${e.message}")
                }
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
