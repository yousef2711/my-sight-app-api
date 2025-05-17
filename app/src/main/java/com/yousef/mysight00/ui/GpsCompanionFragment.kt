package com.yousef.mysight00.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.R
import com.yousef.mysight00.databinding.FragmentGpsCompanionBinding

class GpsCompanionFragment : Fragment() {

    private var _binding: FragmentGpsCompanionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGpsCompanionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {

            unsafeArea.setOnClickListener {
                securityPopup.visibility = View.VISIBLE
            }

            icNotificationComp.setOnClickListener {
                findNavController().navigate(R.id.action_gps_to_notification)
            }

            logoProfileHomeComp.setOnClickListener {
                findNavController().navigate(R.id.action_gps_to_profile)
            }

            securityPopup.setOnClickListener {
                securityPopup.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
