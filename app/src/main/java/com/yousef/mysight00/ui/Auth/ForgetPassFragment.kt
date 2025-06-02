package com.yousef.mysight00.ui.Auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.RetrofitInstance
import com.yousef.mysight00.databinding.FragmentForgetPassBinding
import com.yousef.mysight00.model.forgotPasswordRequest
import com.yousef.mysight00.ui.base.BaseFragment
import com.yousef.mysight00.utils.showToast
import kotlinx.coroutines.launch

class ForgetPassFragment : BaseFragment() {

    private var _binding: FragmentForgetPassBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgetPassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.txtCancelForget.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnDoneForget.setOnClickListener {
            handleForgotPassword()
        }
    }

    private fun handleForgotPassword() {
        val email = binding.phNumForget.text.toString().trim()

        if (validateInput(email)) {
            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.getApi(requireContext()).forgotPassword(forgotPasswordRequest(email))
                    if (response.isSuccessful) {
                        requireContext().showToast("Password reset link has been sent to your email")
                        findNavController().navigateUp()
                    } else {
                        requireContext().showToast("Failed to send password reset link")
                    }
                } catch (e: Exception) {
                    requireContext().showToast("An error occurred: ${e.message}")
                }
            }
        }
    }

    private fun validateInput(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                requireContext().showToast("Please enter your email").let { false }
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                requireContext().showToast("Please enter a valid email").let { false }
            }
            else -> true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
