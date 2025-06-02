package com.yousef.mysight00.ui.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.R
import com.yousef.mysight00.constant
import com.yousef.mysight00.databinding.FragmentHomeBlindBinding
import com.yousef.mysight00.ui.base.BaseFragment
import com.yousef.mysight00.utils.UserPreferences
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig

class HomeBlindFragment : BaseFragment() {

    private var _binding: FragmentHomeBlindBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBlindBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {
            icNotificationBlind.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_notification)
            }
            logoProfileHomeBlind.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_profile)
            }
            icCallBlind.setOnClickListener {
                startAudioCall()
            }
            icVideoBlind.setOnClickListener {
                startVideoCall()
            }
        }
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
