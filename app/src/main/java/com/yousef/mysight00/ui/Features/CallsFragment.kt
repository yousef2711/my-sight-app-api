package com.yousef.mysight00.ui.Features

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yousef.mysight00.MainActivity
import com.yousef.mysight00.R
import com.yousef.mysight00.constant
import com.yousef.mysight00.ui.base.BaseFragment
import com.zegocloud.uikit.ZegoUIKit
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason
import java.util.UUID

class CallsFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calls, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as? MainActivity)?.setBottomNavigationVisibility(false)

        val userID = arguments?.getString("userID") ?: UUID.randomUUID().toString()
        val userName = arguments?.getString("userName") ?: "Unknown"
        val callID = arguments?.getString("callID") ?: "defaultCall"
        val isVideoCall = arguments?.getBoolean("isVideoCall") ?: true

        val callConfig: ZegoUIKitPrebuiltCallConfig = if (isVideoCall) {
            ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall()
        } else {
            ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall()
        }

        // إنشاء الفراجمنت
        val callFragment = ZegoUIKitPrebuiltCallFragment.newInstance(
            constant.appId,
            constant.AppSign,
            userID,
            userName,
            callID,
            callConfig
        )

        // استماع لانتهاء المكالمة
        ZegoUIKit.addRoomStateChangedListener { roomID, reason, _, _ ->
            if (reason == ZegoRoomStateChangedReason.LOGOUT ||
                reason == ZegoRoomStateChangedReason.KICK_OUT ||
                reason == ZegoRoomStateChangedReason.RECONNECT_FAILED
            ) {
                requireActivity().runOnUiThread {
                    parentFragmentManager.popBackStack()
                    (requireActivity() as? MainActivity)?.setBottomNavigationVisibility(true)
                }
            }
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.call_fragment_container, callFragment)
            .addToBackStack(null)
            .commit()
    }
}
