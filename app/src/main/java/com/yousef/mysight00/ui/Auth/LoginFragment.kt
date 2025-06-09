package com.yousef.mysight00.ui.Auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.MainActivity
import com.yousef.mysight00.R
import com.yousef.mysight00.databinding.FragmentLoginBinding
import com.yousef.mysight00.ui.base.BaseFragment
import com.yousef.mysight00.utils.UserPreferences
import com.yousef.mysight00.utils.showToast

class LoginFragment : BaseFragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

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

        // مؤقتاً، فقط fallback بدون محاولة الاتصال بالسيرفر
        val fallbackEmail = email.lowercase()
        val prefs = UserPreferences(requireContext())
        when (fallbackEmail) {
            "blind@test.com" -> {
                prefs.saveUserId("1")
                prefs.saveUserType("patients")
                prefs.saveUserName("blind")
                prefs.saveUsername("blind_user")
                prefs.saveCompanionId("2")
                prefs.saveCompanionName("comp_user")
                navigateToMainActivity()
            }
            "alz@test.com" -> {
                prefs.saveUserId("3")
                prefs.saveUserType("patients")
                prefs.saveUserName("alzheimer")
                prefs.saveUsername("alz_user")
                prefs.saveCompanionId("4")
                navigateToMainActivity()
            }
            "companion@test.com" -> {
                prefs.saveUserId("5")
                prefs.saveUserType("companions")
                prefs.saveUserName("companion")
                prefs.saveUsername("comp_user")
                prefs.savePatientId("1")
                prefs.savePatientName("blind_user")
                navigateToMainActivity()
            }
            else -> {
                requireContext().showToast("البريد غير معروف")
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
