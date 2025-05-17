package com.yousef.mysight00.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.R
import com.yousef.mysight00.databinding.FragmentHomeAlzheimerBinding

class HomeAlzheimerFragment : Fragment(R.layout.fragment_home_alzheimer) {

    private var _binding: FragmentHomeAlzheimerBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeAlzheimerBinding.bind(view)

        binding.apply {
            icNotificationComp.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_notification)
            }

            logoProfileHomeComp.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_profile)
            }

            tvSeeAll.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_tasks)
            }

            listOf(imageCard1Alzh, imageCard2Alzh, imageCard3Alzh, imageCard4Alzh).forEach {
                it.setOnClickListener {
                    findNavController().navigate(R.id.action_home_to_tasks)
                }
            }

            icCallBlind.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_audio_call)
            }

            icVideoBlind.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_video_call)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
