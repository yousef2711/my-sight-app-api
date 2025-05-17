package com.yousef.mysight00.ui

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.R
import com.yousef.mysight00.databinding.FragmentHomeBlindBinding

class HomeBlindFragment : Fragment() {

    private var _binding: FragmentHomeBlindBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBlindBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            logoProfileHomeBlind.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_profile)
            }

            icNotificationBlind.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_notification)
            }

            icCameraBlind.setOnClickListener {
                openCamera()
            }

            icCallBlind.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_audio_call)
            }

            icVideoBlind.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_video_call)
            }
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivity(cameraIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
