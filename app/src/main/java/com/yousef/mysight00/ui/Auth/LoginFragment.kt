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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

        binding.btnLoginLogin.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.getApi(requireContext()).loginUser(loginRequest(email, password))
                binding.progressBar.visibility = View.GONE
                binding.btnLoginLogin.isEnabled = true

                if (!response.isSuccessful || response.body() == null) {
                    requireContext().showToast("فشل تسجيل الدخول: ${response.code()}")
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
                        savePatientName(user.linked_patient_name ?: "")
                        saveCompanionName(user.linked_companion_name ?: "")
                        saveUserType(user.account_type?.lowercase() ?: "")
                    }

                    navigateBasedOnUser(user.account_type?.lowercase(), user.name?.lowercase())
                } ?: requireContext().showToast("فشل في الحصول على بيانات المستخدم")

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnLoginLogin.isEnabled = true
                requireContext().showToast("حدث خطأ: ${e.message}")
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> requireContext().showToast("يرجى إدخال البريد الإلكتروني").let { false }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                requireContext().showToast("يرجى إدخال بريد إلكتروني صحيح")
                false
            }
            password.isEmpty() -> requireContext().showToast("يرجى إدخال كلمة المرور").let { false }
            else -> true
        }
    }

    private fun navigateBasedOnUser(accountType: String?, userName: String?) {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }
}
