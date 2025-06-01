package com.yousef.mysight00.ui.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.R
import com.yousef.mysight00.constant
import com.yousef.mysight00.databinding.FragmentHomeAlzheimerBinding
import com.yousef.mysight00.ui.base.BaseFragment
import com.yousef.mysight00.utils.UserPreferences
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig

class HomeAlzheimerFragment : BaseFragment() {
    private var _binding: FragmentHomeAlzheimerBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences

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
                startAudioCall()
            }
            icVideoAlzheimer.setOnClickListener {
                startVideoCall()
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
