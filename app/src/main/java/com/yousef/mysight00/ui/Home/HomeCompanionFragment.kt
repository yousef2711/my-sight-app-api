package com.yousef.mysight00.ui.Home

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.R
import com.yousef.mysight00.constant
import com.yousef.mysight00.databinding.FragmentHomeCompanionBinding
import com.yousef.mysight00.ui.base.BaseFragment
import com.yousef.mysight00.utils.UserPreferences
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class HomeCompanionFragment : BaseFragment() {
    private var _binding: FragmentHomeCompanionBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences

    // 🧠 نقطة تمثّل موقع المريض (بشكل ثابت مؤقتًا)
    private val patientLocation = GeoPoint(30.0444, 30.9320)

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
                startAudioCall()
            }
            icVideoCompanion.setOnClickListener {
                startVideoCall()
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
        mapController.setZoom(17.0)
        mapController.setCenter(patientLocation)

        // 📍 أضف Marker يمثل المريض
        val patientMarker = Marker(map)
        patientMarker.position = patientLocation
        patientMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        patientMarker.title = "Patient site"
        patientMarker.icon = resources.getDrawable(R.drawable.ic_patient_location, null)
        map.overlays.add(patientMarker)

        map.invalidate()
    }

    private fun startAudioCall() {
        val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
        ZegoUIKitPrebuiltCallService.init(
            requireActivity().application,
            constant.appId,
            constant.AppSign,
            getCurrentUserId(),
            getTargetUserId(),
            callInvitationConfig
        )
    }

    private fun startVideoCall() {
        val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
        ZegoUIKitPrebuiltCallService.init(
            requireActivity().application,
            constant.appId,
            constant.AppSign,
            getCurrentUserId(),
            getTargetUserId(),
            callInvitationConfig
        )
    }

    private fun getCurrentUserId(): String {
        return userPreferences.getUserId() ?: "UnknownID"
    }

    private fun getTargetUserId(): String {
        val userType = userPreferences.getUserType() ?: "companions"
        return if (userType == "companions") {
            userPreferences.getPatientName() ?: "UnknownPatientID"
        } else {
            userPreferences.getCompanionName() ?: "UnknownCompanionID"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
