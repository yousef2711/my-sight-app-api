package com.yousef.mysight00.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.R
import com.yousef.mysight00.RetrofitInstance
import com.yousef.mysight00.databinding.FragmentLoginBinding
import com.yousef.mysight00.model.loginRequest
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

//            when {
//            email == "companion@gmail.com" && password == "123456" -> "companion"
//            email == "alzhaimer@gmail.com" && password == "123456" -> "alzhaimer"
//            email == "blind@gmail.com" && password == "123456" -> "blind"
//            else -> null
//        }
            Log.i("LoginFragment", "Email: $email, Password: $password")
            lifecycleScope.launch {
                val response = RetrofitInstance.api.loginUser(loginRequest(email, password))
                if (response.isSuccessful) {
                    val userType =if ( response.body()?.user?.account_type == "patients") {
                     "blind"
                    } else {
                        "companion"
                    }
                    Log.i("LoginFragment", "User type: $userType")
                    val intent = requireActivity().intent
                    intent.putExtra("user_type", userType)
                    requireActivity().recreate()
                } else {
                    Log.i("LoginFragment", "Login failed: ${response.code()}")
//            requireContext().showToast("البريد الإلكتروني أو كلمة المرور غير صحيحة")
                }
            }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
