package com.yousef.mysight00.ui.Home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.R
import com.yousef.mysight00.constant
import com.yousef.mysight00.databinding.FragmentHomeBlindBinding
import com.yousef.mysight00.ui.base.BaseFragment
import com.yousef.mysight00.utils.UserPreferences
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment

class HomeBlindFragment : BaseFragment() {

    private var _binding: FragmentHomeBlindBinding? = null
    private val binding get() = _binding!!

    private lateinit var userPreferences: UserPreferences

    private val PERMISSION_REQUEST_CODE = 1003

    // لتخزين نوع المكالمة المطلوبة أثناء طلب الصلاحيات
    private var pendingVideoCall: Boolean? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBlindBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreferences = UserPreferences(requireContext())

        binding.icCallBlind.setOnClickListener {
            if (checkAndRequestPermissions(audioOnly = true)) {
                startCall(isVideoCall = false)
            } else {
                pendingVideoCall = false
            }
        }

        binding.icVideoBlind.setOnClickListener {
            if (checkAndRequestPermissions(audioOnly = false)) {
                startCall(isVideoCall = true)
            } else {
                pendingVideoCall = true
            }
        }

        binding.logoProfileHomeBlind.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }
    }

    private fun checkAndRequestPermissions(audioOnly: Boolean): Boolean {
        val requiredPermissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (!audioOnly) requiredPermissions.add(Manifest.permission.CAMERA)

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        return if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), missingPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
            false
        } else {
            true
        }
    }

    private fun startCall(isVideoCall: Boolean) {
        val userName = userPreferences.getUserName() ?: "user"
        val userId = userPreferences.getUserId() ?: "0"
        val targetUserId = userPreferences.getCompanionId() ?: run {
            userId
        }

        try {
            val callConfig = if (isVideoCall) {
                ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall()
            } else {
                ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall()
            }

            val callFragment = ZegoUIKitPrebuiltCallFragment.newInstance(
                constant.appId,
                constant.AppSign,
                userId,
                userName,
                targetUserId,
                callConfig
            )

            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, callFragment)
                .addToBackStack(null)
                .commit()

            showToast("جاري بدء المكالمة...")
        } catch (e: Exception) {
            showToast("حدث خطأ أثناء بدء المكالمة: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                pendingVideoCall?.let {
                    startCall(it)
                    pendingVideoCall = null
                }
            } else {
                // بإمكانك عرض رسالة للمستخدم هنا
                // مثال:
                // Toast.makeText(requireContext(), "يجب منح صلاحيات المايكروفون والكاميرا للاتصال", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
