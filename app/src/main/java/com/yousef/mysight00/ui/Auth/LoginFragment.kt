package com.yousef.mysight00.ui.Auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.MainActivity
import com.yousef.mysight00.R
import com.yousef.mysight00.RetrofitInstance
import com.yousef.mysight00.databinding.FragmentLoginBinding
import com.yousef.mysight00.model.loginRequest
import com.yousef.mysight00.ui.base.BaseFragment
import com.yousef.mysight00.utils.UserPreferences
import com.yousef.mysight00.utils.showToast
import kotlinx.coroutines.launch


class LoginFragment : BaseFragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val availableAvatars = listOf(
        R.drawable.img_edit_profile,
        R.drawable.ic_personal_profile,
        R.drawable.img_personal,
        R.drawable.img_personal1
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupClickListeners()
    }

    private fun setupClickListeners() = with(binding) {
        btnSignupLog.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
        tvForgetPassLogin.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forget_password)
        }
        btnLoginLogin.setOnClickListener {
            handleLogin()
        }
    }

    private fun handleLogin() {
        val email = binding.emailLogin.text.toString().trim()
        val password = binding.passwordLogin.text.toString().trim()

        if (!validateInputs(email, password)) return

        setLoadingState(isLoading = true)

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.getApi(requireContext())
                    .loginUser(loginRequest(email, password))

                if (!response.isSuccessful || response.body() == null) {
                    val errorMsg = response.errorBody()?.string() ?: "Login failed: ${response.code()}"
                    requireContext().showToast(errorMsg)
                    return@launch
                }

                val loginResponse = response.body()!!
                val prefs = UserPreferences(requireContext())

                loginResponse.access?.let { prefs.saveAccessToken(it) }
                loginResponse.refresh?.let { prefs.saveRefreshToken(it) }

                loginResponse.user?.let { user ->
                    prefs.apply {
                        saveUserId(user.id?.toString() ?: "0")
                        saveUserName(user.name ?: "")
                        saveUsername(user.username ?: "")
                        saveUserEmail(user.email ?: "")
                        saveUserPhone(user.phone_number ?: "")
                        saveUserRelationship(user.relationship ?: "")
                        savePatientName(user.patient_username ?: "")
                        saveCompanionName(user.linked_companion_name ?: "")
                        savePatientType(user.linked_patient_type ?: "")
                        saveUserType(user.account_type?.lowercase() ?: "")

                        // حفظ معرفات المستخدمين المرتبطين
                        if (user.account_type?.lowercase() == "companions") {
                            savePatientId(user.patient_username?.toString() ?: user.patient_username ?: "")
                        } else {
                            saveCompanionId(user.linked_companion_name?.toString() ?: "")
                        }

                        // Assign random avatar if user doesn't have one
                        if (getUserAvatar() == null) {
                            val randomAvatar = availableAvatars.random()
                            saveUserAvatar(randomAvatar)
                        }
                    }

                    navigateToMainActivity()
                } ?: requireContext().showToast("Failed to get user data")

            } catch (e: Exception) {
                requireContext().showToast("An error occurred: ${e.message}")
            } finally {
                setLoadingState(isLoading = false)
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) = with(binding) {
        btnLoginLogin.isEnabled = !isLoading
        emailLogin.isEnabled = !isLoading
        passwordLogin.isEnabled = !isLoading
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            requireContext().showToast("Please enter your email")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            requireContext().showToast("Please enter a valid email")
            return false
        }
        if (password.isEmpty()) {
            requireContext().showToast("Please enter your password")
            return false
        }
        return true
    }

    private fun navigateToMainActivity() {
        val prefs = UserPreferences(requireContext())
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("userType", prefs.getUserName())     // Companion, blind, alzheimer
            putExtra("userName", prefs.getUsername())     // لعرضه في المكالمة مثلاً
            putExtra("userId", prefs.getUserId())         // قد تحتاجه في زيجو
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
