package com.yousef.mysight00.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.R
import com.yousef.mysight00.databinding.FragmentHomeCompanionBinding

class HomeCompanionFragment : Fragment() {
    private var _binding: FragmentHomeCompanionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeCompanionBinding.inflate(inflater, container, false)
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
            
            imageGpsComp.setOnClickListener { 
                findNavController().navigate(R.id.action_home_to_gps)
            }
            
            icCallBlind.setOnClickListener { 
                findNavController().navigate(R.id.action_home_to_audio_call)
            }
            
            icVideoBlind.setOnClickListener { 
                findNavController().navigate(R.id.action_home_to_video_call)
            }
            
            tvSeeAll.setOnClickListener { 
                findNavController().navigate(R.id.action_home_to_tasks)
            }
            
            listOf(imageCard1Comp, imageCard2Comp, imageCard3Comp).forEach {
                it.setOnClickListener { 
                    findNavController().navigate(R.id.action_home_to_tasks)
                }
            }

            bottomNavigationView5.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_gps -> {
                        findNavController().navigate(R.id.action_home_to_gps)
                        true
                    }
                    R.id.nav_calls -> {
                        findNavController().navigate(R.id.action_home_to_audio_call)
                        true
                    }
                    R.id.nav_task -> {
                        findNavController().navigate(R.id.action_home_to_tasks)
                        true
                    }
                    R.id.nav_history -> {
                        findNavController().navigate(R.id.action_home_to_history)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
