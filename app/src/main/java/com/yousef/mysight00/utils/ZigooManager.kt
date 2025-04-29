package com.yousef.mysight00.utils

import android.app.Application
import android.content.Context
import android.view.TextureView
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import im.zego.zegoexpress.ZegoExpressEngine
import im.zego.zegoexpress.callback.IZegoEventHandler
import im.zego.zegoexpress.constants.ZegoScenario
import im.zego.zegoexpress.constants.ZegoUpdateType
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason
import im.zego.zegoexpress.entity.ZegoCanvas
import im.zego.zegoexpress.entity.ZegoEngineProfile
import im.zego.zegoexpress.entity.ZegoUser
import im.zego.zegoexpress.entity.ZegoRoomConfig
import org.json.JSONObject
import java.util.*

class ZigooManager private constructor(context: Context) {

    private var engine: ZegoExpressEngine? = null
    private var isVideoEnabled = true
    private var isMuted = false
    private var isSpeakerOn = true
    private val appContext = context.applicationContext

    companion object {
        private const val APP_ID: Long = 1656211520L
        private const val APP_SIGN = "01a2e2c7abf014b457cc05cc6f39d9271920e292d643fd8c70bc233ef27808f4"
        private var instance: ZigooManager? = null

        @Synchronized
        fun getInstance(context: Context): ZigooManager {
            if (instance == null) {
                instance = ZigooManager(context)
            }
            return instance!!
        }
    }

    init {
        createEngine()
    }

    private fun createEngine() {
        val profile = ZegoEngineProfile().apply {
            appID = APP_ID
            appSign = APP_SIGN
            scenario = ZegoScenario.GENERAL
            application = appContext as Application
        }

        engine = ZegoExpressEngine.createEngine(profile, object : IZegoEventHandler() {
            override fun onRoomUserUpdate(
                roomID: String?,
                updateType: ZegoUpdateType?,
                userList: ArrayList<ZegoUser>?
            ) {
                super.onRoomUserUpdate(roomID, updateType, userList)
            }

            override fun onRoomOnlineUserCountUpdate(roomID: String?, count: Int) {
                super.onRoomOnlineUserCountUpdate(roomID, count)
            }

            override fun onRoomStateChanged(
                roomID: String?,
                reason: ZegoRoomStateChangedReason?,
                errorCode: Int,
                extendedData: JSONObject?
            ) {
                super.onRoomStateChanged(roomID, reason, errorCode, extendedData)
            }

            override fun onDebugError(errorCode: Int, funcName: String, info: String?) {
                super.onDebugError(errorCode, funcName, info)
            }
        })
    }

    fun joinChannel(channelId: String, userId: Int, enableVideo: Boolean) {
        val user = ZegoUser(userId.toString())
        val config = ZegoRoomConfig()
        engine?.loginRoom(channelId, user, config)
        if (enableVideo) {
            engine?.enableCamera(true)
            engine?.mutePublishStreamAudio(false)
            engine?.mutePlayStreamAudio("main", false)
        }
    }

    fun setupLocalVideo(textureView: TextureView) {
        val canvas = ZegoCanvas(textureView)
        engine?.startPreview(canvas)
    }

    fun leaveChannel() {
        engine?.stopPreview()
        engine?.logoutRoom()
    }

    fun toggleVideo() {
        isVideoEnabled = !isVideoEnabled
        engine?.enableCamera(isVideoEnabled)
    }

    fun toggleMute() {
        isMuted = !isMuted
        engine?.mutePublishStreamAudio(isMuted)
    }

    fun toggleSpeaker() {
        isSpeakerOn = !isSpeakerOn
        engine?.mutePlayStreamAudio("main", !isSpeakerOn)
    }

    fun destroy() {
        ZegoExpressEngine.destroyEngine(null)
        instance = null
    }
}
