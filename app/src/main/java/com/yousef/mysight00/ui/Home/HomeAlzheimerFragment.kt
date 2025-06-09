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
import com.yousef.mysight00.databinding.FragmentHomeAlzheimerBinding
import com.yousef.mysight00.ui.base.BaseFragment
import com.yousef.mysight00.utils.UserPreferences
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment

class HomeAlzheimerFragment : BaseFragment() {

    private var _binding: FragmentHomeAlzheimerBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences

    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeAlzheimerBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {
            icNotificationComp.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_notification)
            }
            logoProfileHomeComp.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_profile)
            }
            icCallAlzheimer.setOnClickListener {
                // تحقق من صلاحيات الصوت وابدأ مكالمة صوتية
                if (checkAndRequestPermissions(audioOnly = true)) {
                    startCall(isVideoCall = false)
                }
            }
            icVideoAlzheimer.setOnClickListener {
                // تحقق من صلاحيات الصوت والكاميرا وابدأ مكالمة فيديو
                if (checkAndRequestPermissions(audioOnly = false)) {
                    startCall(isVideoCall = true)
                }
            }
            tvSeeAll.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_tasks)
            }
            listOf(imageCard1Alzh, imageCard2Alzh, imageCard3Alzh).forEach {
                it.setOnClickListener {
                    findNavController().navigate(R.id.action_home_to_tasks)
                }
            }
        }
    }

    private fun checkAndRequestPermissions(audioOnly: Boolean): Boolean {
        val requiredPermissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (!audioOnly) {
            requiredPermissions.add(Manifest.permission.CAMERA)
        }

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
        val targetUserId = getTargetUserId()

        if (userId == "UnknownID" || targetUserId == "UnknownID") {
            showToast("لم يتم العثور على معرف المستخدم أو المستخدم المستهدف")
            return
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

    private fun getCurrentUserId(): String {
        return userPreferences.getUserId() ?: "UnknownID"
    }

    private fun getTargetUserId(): String {
        val userType = userPreferences.getUserType() ?: "companions"
        return if (userType == "companions") {
            // إذا كان المستخدم مرافق، نحتاج إلى الحصول على معرف المريض
            val patientId = userPreferences.getPatientId()
            if (patientId.isNullOrEmpty() || patientId == "0") {
                // إذا لم يكن هناك معرف للمريض، نستخدم معرف المستخدم الحالي
                userPreferences.getUserId() ?: "UnknownID"
            } else {
                patientId
            }
        } else {
            // إذا كان المستخدم مريض، نحتاج إلى الحصول على معرف المرافق
            val companionId = userPreferences.getCompanionId()
            if (companionId.isNullOrEmpty() || companionId == "0") {
                // إذا لم يكن هناك معرف للمرافق، نستخدم معرف المستخدم الحالي
                userPreferences.getUserId() ?: "UnknownID"
            } else {
                companionId
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ** تعامل مع رد صلاحيات المستخدم **
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // الصلاحيات كلها تم منحها، أعد محاولة بدء المكالمة (يمكن تعديل لتمرير حالة المكالمة)
            } else {
                // إعلام المستخدم بضرورة السماح بالصلاحيات
                showToast("يجب منح صلاحيات الميكروفون والكاميرا للاتصال")
            }
        }
    }
}
