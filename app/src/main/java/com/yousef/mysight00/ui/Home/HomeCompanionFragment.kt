package com.yousef.mysight00.ui.Home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.R
import com.yousef.mysight00.constant
import com.yousef.mysight00.databinding.FragmentHomeCompanionBinding
import com.yousef.mysight00.ui.base.BaseFragment
import com.yousef.mysight00.utils.UserPreferences
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class HomeCompanionFragment : BaseFragment() {
    private var _binding: FragmentHomeCompanionBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences

    private val PERMISSION_REQUEST_CODE = 1002

    // موقع المريض (ثابت مؤقتًا)
    private val patientLocation = GeoPoint(30.0444, 30.9320)

    // لتخزين نوع المكالمة المطلوبة عند طلب الصلاحيات
    private var pendingVideoCall: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeCompanionBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupMap()
    }

    private fun setupClickListeners() {
        binding.apply {
            icNotificationComp.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_notification)
            }
            logoProfileHomeComp.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_profile)
            }
            imageGpsComp.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_gps)
            }
            icCallCompanion.setOnClickListener {
                // طلب صلاحيات المايكروفون لمكالمة صوتية
                if (checkAndRequestPermissions(audioOnly = true)) {
                    startCall(isVideoCall = false)
                } else {
                    pendingVideoCall = false
                }
            }
            icVideoCompanion.setOnClickListener {
                // طلب صلاحيات المايكروفون والكاميرا لمكالمة فيديو
                if (checkAndRequestPermissions(audioOnly = false)) {
                    startCall(isVideoCall = true)
                } else {
                    pendingVideoCall = true
                }
            }
            tvSeeAll.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_tasks)
            }
            listOf(imageCard1Comp, imageCard2Comp, imageCard3Comp).forEach {
                it.setOnClickListener {
                    findNavController().navigate(R.id.action_home_to_tasks)
                }
            }
        }
    }

    private fun setupMap() {
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))

        val map = binding.imageGpsComp
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)

        val mapController = map.controller
        mapController.setZoom(14.0)  // تكبير أوسع للخريطة
        mapController.setCenter(patientLocation)

        val patientMarker = Marker(map)
        patientMarker.position = patientLocation
        patientMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        patientMarker.title = "Patient site"
        patientMarker.icon = resources.getDrawable(R.drawable.ic_patient_location, null)
        map.overlays.add(patientMarker)

        // إضافة Overlay للاستماع للنقرات على الخريطة
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                findNavController().navigate(R.id.action_home_to_gps)
                return true
            }
            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }
        val mapEventsOverlay = MapEventsOverlay(requireContext(), mapEventsReceiver)
        map.overlays.add(mapEventsOverlay)

        map.invalidate()
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
            userPreferences.getCompanionId() ?: "UnknownID"
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // صلاحيات مُنحت، نبدأ المكالمة حسب نوعها المحفوظ
                pendingVideoCall?.let {
                    startCall(it)
                    pendingVideoCall = null
                }
            } else {
                showToast("يجب منح صلاحيات المايكروفون والكاميرا للاتصال")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
