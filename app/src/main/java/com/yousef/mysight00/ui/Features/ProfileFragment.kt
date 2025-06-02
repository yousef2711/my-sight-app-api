package com.yousef.mysight00.ui.Features

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yousef.mysight00.MainActivity
import com.yousef.mysight00.R
import com.yousef.mysight00.adapter.ProfileAdapter
import com.yousef.mysight00.model.ProfileItem
import com.yousef.mysight00.model.UserType
import com.yousef.mysight00.ui.base.BaseFragment
import com.yousef.mysight00.utils.UserPreferences

class ProfileFragment : BaseFragment() {
    private val TAG = "ProfileFragment"
    private lateinit var userPreferences: UserPreferences
    private val avatars = listOf(
        R.drawable.img_edit_profile,
        R.drawable.ic_personal_profile,
        R.drawable.img_personal,
        R.drawable.img_personal1
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }

        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        userPreferences = UserPreferences(requireContext())

        updateUserProfile(view)

        view.findViewById<ImageView>(R.id.profileImage).setOnClickListener {
            showAvatarSelectionDialog()
        }

        view.findViewById<ImageView>(R.id.cameraButton).setOnClickListener {
            showAvatarSelectionDialog()
        }

        setupProfileList(view)

        view.findViewById<View>(R.id.editProfileButton)?.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_edit_profile)
        }

        view.findViewById<View>(R.id.backButton_history)?.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    private fun setupProfileList(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val items = mutableListOf(
            ProfileItem(R.drawable.ic_profile, "Name", userPreferences.getUsername() ?: "Not Set"),
            ProfileItem(R.drawable.ic_email, "Email", userPreferences.getUserEmail() ?: "Not Set"),
            ProfileItem(R.drawable.ic_phone, "Phone", userPreferences.getUserPhone() ?: "Not Set"),
            ProfileItem(R.drawable.ic_location, "Location", userPreferences.getUserLocation() ?: "Not Set")
        )

        val userName = userPreferences.getUserName()?.lowercase() ?: ""
        when {
            userName.contains("companions") -> {
                items.add(ProfileItem(R.drawable.ic_relation, "Relationship", userPreferences.getUserRelationship() ?: "Not Set"))
                items.add(ProfileItem(R.drawable.ic_rela_name, "Patient Name", userPreferences.getPatientName() ?: "Not Set"))
                items.add(ProfileItem(R.drawable.ic_rela_name, "Patient Type", userPreferences.getPatientType() ?: "Not Set"))
            }
            userName.contains("alzheimer") -> {
                items.add(ProfileItem(R.drawable.ic_relation, "Relationship", userPreferences.getUserRelationship() ?: "Not Set"))
                items.add(ProfileItem(R.drawable.ic_rela_name, "Companion Name", userPreferences.getCompanionName() ?: "Not Set"))
            }
            userName.contains("blind") -> {
                items.add(ProfileItem(R.drawable.ic_relation, "Relationship", userPreferences.getUserRelationship() ?: "Not Set"))
                items.add(ProfileItem(R.drawable.ic_rela_name, "Companion Name", userPreferences.getCompanionName() ?: "Not Set"))
            }
            else -> {}
        }

        items.add(ProfileItem(R.drawable.ic_logout, "Log Out"))

        val adapter = ProfileAdapter(items, userPreferences) { item ->
            when (item.title) {
                "Log Out" -> showLogoutConfirmationDialog()
            }
        }
        recyclerView.adapter = adapter
    }

    private fun showLogoutConfirmationDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_logout_confirmation)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<View>(R.id.btnCancel)?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<View>(R.id.btnConfirm)?.setOnClickListener {
            dialog.dismiss()
            handleLogout()
        }

        dialog.show()
    }

    private fun showAvatarSelectionDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_avatar_selection)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        avatars.forEachIndexed { index, avatarResId ->
            dialog.findViewById<ImageView>(getAvatarViewId(index))?.setOnClickListener {
                userPreferences.saveUserAvatar(avatarResId)
                view?.findViewById<ImageView>(R.id.profileImage)?.setImageResource(avatarResId)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun getAvatarViewId(index: Int): Int {
        return when (index) {
            0 -> R.id.avatar1
            1 -> R.id.avatar2
            2 -> R.id.avatar3
            3 -> R.id.avatar4
            else -> throw IllegalArgumentException("Invalid avatar index")
        }
    }

    private fun updateUserProfile(view: View) {
        // تحديث صورة البروفايل
        val profileImage = view.findViewById<ImageView>(R.id.profileImage)
        val savedAvatar = userPreferences.getUserAvatar()
        if (savedAvatar != null) {
            profileImage.setImageResource(savedAvatar)
        }

        val userNameTextView = view.findViewById<TextView>(R.id.userName)
        userNameTextView.text = userPreferences.getUsername() ?: "UserName"

        val userTypeTextView = view.findViewById<TextView>(R.id.userTybe)
        val userType = UserType.fromString(userPreferences.getUserName() ?: "")
        userTypeTextView.text = userType?.toString() ?: ""
    }

    private fun handleLogout() {
        try {
            Log.d(TAG, "Starting logout process")
            
            Log.d(TAG, "Clearing user data")
            userPreferences.clearAll()
            Log.d(TAG, "User data cleared successfully")

            // Restart app to return to login screen
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("SKIP_SPLASH", true)  // Add flag to skip splash screen
            }
            startActivity(intent)
            requireActivity().finish()
            Log.d(TAG, "App restarted")
        } catch (e: Exception) {
            Log.e(TAG, "Error occurred during logout", e)
            e.printStackTrace()
        }
    }

}
