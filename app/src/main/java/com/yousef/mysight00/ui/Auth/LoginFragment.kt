package com.yousef.mysight00.ui.Auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.MainActivity
import com.yousef.mysight00.R
import com.yousef.mysight00.RetrofitInstance
import com.yousef.mysight00.databinding.FragmentLoginBinding
import com.yousef.mysight00.model.UserType
import com.yousef.mysight00.model.loginRequest
import com.yousef.mysight00.utils.UserPreferences
import com.yousef.mysight00.utils.showToast
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

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
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {
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
    }

    private fun handleLogin() {
        val email = binding.emailLogin.text.toString().trim()
        val password = binding.passwordLogin.text.toString().trim()

        if (validateInputs(email, password)) {
            authenticateUser(email, password)
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                requireContext().showToast("يرجى إدخال البريد الإلكتروني")
                false
            }
            password.isEmpty() -> {
                requireContext().showToast("يرجى إدخال كلمة المرور")
                false
            }
            //commented for testing only
//            !email.contains("@") -> {
//                requireContext().showToast("البريد الإلكتروني غير صحيح")
//                false
//            }
            else -> true
        }
    }

    private fun authenticateUser(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.loginUser(loginRequest(email, password))
                if (response.isSuccessful) {
                    val user = response.body()?.user

                    val userType: UserType? = when (user?.name?.lowercase()) {
                        "blind" -> UserType.BLIND
                        "alzheimer" -> UserType.ALZHEIMER
                        "companion" -> UserType.COMPANION
                        else -> null
                    }

                    if (user != null && userType != null) {
                        Log.i("LoginFragment", "User type: $userType")

                        // حفظ بيانات المستخدم
                        val userPreferences = UserPreferences(requireContext())
                        userPreferences.saveUserType(userType.nameValue)
                        userPreferences.saveUserId(user.phone_number.toString())

                        // إذا كان المستخدم مرافق، احفظ معرف المريض
                        if (userType == UserType.COMPANION) {
                            userPreferences.savePatientId(user.patient_username ?: "")
                        }

                        // إنشاء intent للذهاب إلى MainActivity
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        intent.putExtra("user_type", userType.nameValue)
                        intent.putExtra("user_id", user.phone_number)
                        intent.putExtra("user_name", user.email)
                        if (userType == UserType.COMPANION) {
                            intent.putExtra("target_user_id", user.patient_username)
                        }

                        startActivity(intent)
                        requireActivity().finish()
                    } else {
                        requireContext().showToast("فشل تسجيل الدخول: نوع المستخدم غير معروف")
                        Log.e("LoginFragment", "Unknown user: $user")
                    }
                } else {
                    requireContext().showToast("فشل تسجيل الدخول: ${response.code()}")
                    Log.i("LoginFragment", "Login failed: ${response.code()}")
                }
            } catch (e: Exception) {
                requireContext().showToast("حدث خطأ أثناء تسجيل الدخول")
                Log.e("LoginFragment", "Login error", e)
            }
        }
    }

    /*
       Log.i("LoginFragment", "Email: $email, Password: $password")

        // Temporary hardcoded authentication
        val userType = when {
            email == "companion@gmail.com" && password == "123456" -> "companions"
            email == "alzheimer@gmail.com" && password == "123456" -> "alzheimer"
            email == "blind@gmail.com" && password == "123456" -> "blind"
            else -> null
        }

        if (userType != null) {
            Log.i("LoginFragment", "User type: $userType")

            // حفظ بيانات المستخدم
            val userPreferences = UserPreferences(requireContext())
            userPreferences.saveUserType(userType)
            userPreferences.saveUserId("123456789") // Temporary hardcoded ID

            // إذا كان المستخدم مرافق، احفظ معرف المريض
            if (userType == "companions") {
                userPreferences.savePatientId("patient123") // Temporary hardcoded patient ID
            }

            // إنشاء intent للذهاب إلى MainActivity
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.putExtra("user_type", userType)
            intent.putExtra("user_id", "123456789")
            intent.putExtra("user_name", email)
            if (userType == "companions") {
                intent.putExtra("target_user_id", "patient123")
            }

            startActivity(intent)
            requireActivity().finish()
        } else {
            requireContext().showToast("فشل تسجيل الدخول: بيانات غير صحيحة")
            Log.e("LoginFragment", "Invalid credentials")
        }

    Original API authentication code
    lifecycleScope.launch {
        try {
            val response = RetrofitInstance.api.loginUser(loginRequest(email, password))
            if (response.isSuccessful) {
                val user = response.body()?.user

                val userType: UserType? = when (user?.name?.lowercase()) {
                    "blind" -> UserType.BLIND
                    "alzheimer" -> UserType.ALZHEIMER
                    "companion" -> UserType.COMPANION
                    else -> null
                }

                if (user != null && userType != null) {
                    Log.i("LoginFragment", "User type: $userType")

                    // حفظ بيانات المستخدم
                    val userPreferences = UserPreferences(requireContext())
                    userPreferences.saveUserType(userType.nameValue)
                    userPreferences.saveUserId(user.phone_number.toString())

                    // إذا كان المستخدم مرافق، احفظ معرف المريض
                    if (userType == UserType.COMPANION) {
                        userPreferences.savePatientId(user.patient_username ?: "")
                    }

                    // إنشاء intent للذهاب إلى MainActivity
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.putExtra("user_type", userType.nameValue)
                    intent.putExtra("user_id", user.phone_number)
                    intent.putExtra("user_name", user.email)
                    if (userType == UserType.COMPANION) {
                        intent.putExtra("target_user_id", user.patient_username)
                    }

                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    requireContext().showToast("فشل تسجيل الدخول: نوع المستخدم غير معروف")
                    Log.e("LoginFragment", "Unknown user: $user")
                }
            } else {
                requireContext().showToast("فشل تسجيل الدخول: ${response.code()}")
                Log.i("LoginFragment", "Login failed: ${response.code()}")
            }
        } catch (e: Exception) {
            requireContext().showToast("حدث خطأ أثناء تسجيل الدخول")
            Log.e("LoginFragment", "Login error", e)
        }
    }
    */

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
