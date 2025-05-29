package com.yousef.mysight00.ui.Auth

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.yousef.mysight00.R

class NewPasswordFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_new_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val passwordField = view.findViewById<EditText>(R.id.password1_newpass)
        val confirmPasswordField = view.findViewById<EditText>(R.id.password2_newpass)
        val showPasswordCheckbox = view.findViewById<CheckBox>(R.id.showPasswordCheckbox_newpass)
        val verifyButton = view.findViewById<MaterialButton>(R.id.btnVerify_newpass)
        val successPopup = view.findViewById<ImageView>(R.id.successPopup)

        fun hideKeyboard(view: View) {
            val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        showPasswordCheckbox.setOnCheckedChangeListener { _, isChecked ->
            val transformationMethod = if (isChecked) {
                HideReturnsTransformationMethod.getInstance()
            } else {
                PasswordTransformationMethod.getInstance()
            }
            passwordField.transformationMethod = transformationMethod
            confirmPasswordField.transformationMethod = transformationMethod

            passwordField.setSelection(passwordField.text.length)
            confirmPasswordField.setSelection(confirmPasswordField.text.length)
        }

        verifyButton.setOnClickListener {
            hideKeyboard(it)

            val password = passwordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()

            when {
                password.isEmpty() || confirmPassword.isEmpty() -> {
                    Toast.makeText(requireContext(), "Please enter both fields", Toast.LENGTH_SHORT).show()
                }
                password.length < 6 -> {
                    Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                }
                password != confirmPassword -> {
                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(requireContext(), "Password changed successfully!", Toast.LENGTH_SHORT).show()

                    successPopup.visibility = View.VISIBLE
                    val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.success_popup_animation)
                    successPopup.startAnimation(animation)

                    Handler(Looper.getMainLooper()).postDelayed({
                        findNavController().navigate(R.id.action_new_password_to_login)
                    }, 2500)
                }
            }
        }
    }
}
