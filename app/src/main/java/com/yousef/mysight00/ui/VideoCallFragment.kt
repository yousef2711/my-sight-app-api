package com.yousef.mysight00.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.view.Gravity
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.R
import com.yousef.mysight00.databinding.FragmentVideoCallBinding
import com.yousef.mysight00.utils.ZigooManager
import com.yousef.mysight00.utils.showToast

class VideoCallFragment : Fragment() {

    private var _binding: FragmentVideoCallBinding? = null
    private val binding get() = _binding!!
    private lateinit var zigooManager: ZigooManager
    private var isCallActive = false
    private var isMuted = false
    private var isVideoOn = true
    private var isSpeakerOn = true

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            startCall()
        } else {
            requireContext().showToast("يجب السماح بالوصول إلى الكاميرا والميكروفون لإجراء المكالمة")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoCallBinding.inflate(inflater, container, false)
        zigooManager = ZigooManager.getInstance(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupVideoView()
    }

    private fun setupVideoView() {
        val videoView = binding.root.findViewById<TextureView>(R.id.local_video_view)
        if (videoView != null) {
            val layoutParams = FrameLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.local_video_width),
                resources.getDimensionPixelSize(R.dimen.local_video_height)
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                setMargins(
                    resources.getDimensionPixelSize(R.dimen.local_video_margin),
                    resources.getDimensionPixelSize(R.dimen.local_video_margin),
                    resources.getDimensionPixelSize(R.dimen.local_video_margin),
                    resources.getDimensionPixelSize(R.dimen.local_video_margin)
                )
            }
            videoView.layoutParams = layoutParams
            zigooManager.setupLocalVideo(videoView)
        } else {
            requireContext().showToast("خطأ في إعداد الفيديو")
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnEndCall.setOnClickListener {
                endCall()
            }

            btnMice.setOnClickListener {
                toggleMute()
            }

            btnCamera.setOnClickListener {
                toggleVideo()
            }

            btnSpeaker.setOnClickListener {
                toggleSpeaker()
            }
        }
    }

    private fun startCall() {
        try {
            val channelName = "video_channel_${System.currentTimeMillis()}"
            zigooManager.joinChannel(channelName, 0, true)
            isCallActive = true
            updateUI()
        } catch (e: Exception) {
            requireContext().showToast("حدث خطأ أثناء بدء المكالمة")
        }
    }

    private fun endCall() {
        try {
            zigooManager.leaveChannel()
            isCallActive = false
            updateUI()
            findNavController().navigateUp()
        } catch (e: Exception) {
            requireContext().showToast("حدث خطأ أثناء إنهاء المكالمة")
        }
    }

    private fun toggleMute() {
        try {
            zigooManager.toggleMute()
            isMuted = !isMuted
            updateUI()
        } catch (e: Exception) {
            requireContext().showToast("حدث خطأ أثناء تبديل حالة الميكروفون")
        }
    }

    private fun toggleVideo() {
        try {
            zigooManager.toggleVideo()
            isVideoOn = !isVideoOn
            updateUI()
        } catch (e: Exception) {
            requireContext().showToast("حدث خطأ أثناء تبديل حالة الكاميرا")
        }
    }

    private fun toggleSpeaker() {
        try {
            zigooManager.toggleSpeaker()
            isSpeakerOn = !isSpeakerOn
            updateUI()
        } catch (e: Exception) {
            requireContext().showToast("حدث خطأ أثناء تبديل حالة السماعة")
        }
    }

    private fun updateUI() {
        binding.apply {
            btnMice.setIconResource(
                if (isMuted) R.drawable.ic_mice
                else R.drawable.ic_mice
            )
            btnCamera.setIconResource(
                if (isVideoOn) R.drawable.ic_camera_video
                else R.drawable.ic_camera_video
            )
            btnSpeaker.setIconResource(
                if (isSpeakerOn) R.drawable.ic_speaker
                else R.drawable.ic_speaker
            )
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            startCall()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
