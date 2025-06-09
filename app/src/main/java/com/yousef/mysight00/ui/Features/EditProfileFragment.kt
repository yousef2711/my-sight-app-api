package com.yousef.mysight00.ui.Features

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.R
import com.yousef.mysight00.RetrofitInstance
import com.yousef.mysight00.databinding.FragmentEditProfileBinding
import com.yousef.mysight00.model.EditProfileRequest
import com.yousef.mysight00.ui.base.BaseFragment
import com.yousef.mysight00.utils.UserPreferences
import kotlinx.coroutines.launch

class EditProfileFragment : BaseFragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences
    private var selectedProfilePhoto: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        loadCurrentUserData()
    }

    private fun setupViews() {
        // Set up avatar selection dialog
        binding.btnEditProfile.setOnClickListener {
            showAvatarSelectionDialog()
        }

        // Back button
        binding.arrowBackPatientForm.setOnClickListener {
            findNavController().navigateUp()
        }

        // Save button
        binding.btnCreateProfile.setOnClickListener {
            updateProfile()
        }
    }

    private fun loadCurrentUserData() {
        binding.namePatientForm.setText(userPreferences.getUsername())
        binding.emailEditProfile.setText(userPreferences.getUserEmail())
        binding.phonePatientForm.setText(userPreferences.getUserPhone())
        binding.relativePatientForm.setText(userPreferences.getUserName())
    }

    private fun showAvatarSelectionDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_avatar_selection)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val avatars = listOf(
            R.id.avatar1 to R.drawable.img_edit_profile,
            R.id.avatar2 to R.drawable.ic_personal_profile,
            R.id.avatar3 to R.drawable.img_personal,
            R.id.avatar4 to R.drawable.img_personal1
        )

        avatars.forEach { (id, drawable) ->
            dialog.findViewById<View>(id)?.setOnClickListener {
                binding.imgProfilePatientForm.setImageResource(drawable)
                selectedProfilePhoto = drawable.toString()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun updateProfile() {
        val username = binding.namePatientForm.text.toString()
        val email = binding.emailEditProfile.text.toString()
        val phone = binding.phonePatientForm.text.toString()
        val relative = binding.relativePatientForm.text.toString()

        if (validateInputs(username, email, phone)) {
            lifecycleScope.launch {
                try {
                    val request = EditProfileRequest(
                        username = userPreferences.getUsername() ?: "",
                        phone_number = phone,
                        name = relative,
                        profile_photo = selectedProfilePhoto
                    )

                    val accessToken = userPreferences.getAccessToken()
                    if (accessToken == null) {
                        Toast.makeText(requireContext(), "يرجى تسجيل الدخول مرة أخرى", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val response = RetrofitInstance.getApi(requireContext()).editProfile(
                        token = "Bearer $accessToken",
                        request = request
                    )

                    if (response.isSuccessful) {
                        val updatedUser = response.body()
                        if (updatedUser != null) {
                            userPreferences.saveUserName(updatedUser.name)
                            userPreferences.saveUserPhone(updatedUser.phone_number)

                            Toast.makeText(requireContext(), "تم تحديث الملف الشخصي بنجاح", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                    } else {
                        Toast.makeText(requireContext(), "فشل تحديث الملف الشخصي", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateInputs(name: String, email: String, phone: String): Boolean {
        return when {
            name.isEmpty() -> {
                Toast.makeText(requireContext(), "الرجاء إدخال الاسم", Toast.LENGTH_SHORT).show()
                false
            }
            email.isEmpty() -> {
                Toast.makeText(requireContext(), "الرجاء إدخال البريد الإلكتروني", Toast.LENGTH_SHORT).show()
                false
            }
            phone.isEmpty() -> {
                Toast.makeText(requireContext(), "الرجاء إدخال رقم الهاتف", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
