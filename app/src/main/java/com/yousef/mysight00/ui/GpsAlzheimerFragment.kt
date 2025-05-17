package com.yousef.mysight00.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.R
import com.yousef.mysight00.databinding.FragmentGpsAlzheimerBinding

class GpsAlzheimerFragment : Fragment() {

    private var _binding: FragmentGpsAlzheimerBinding? = null
    private val binding get() = _binding!!
    private var currentState = State.UNKNOWN

    private enum class State {
        UNKNOWN, SAFE, UNSAFE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGpsAlzheimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {
            icNotificationComp.setOnClickListener {
                findNavController().navigate(R.id.action_gps_to_notification)
            }

            logoProfileHomeComp.setOnClickListener {
                findNavController().navigate(R.id.action_gps_to_profile)
            }

            icAreaStatus.setOnClickListener {
                changeAreaState()
            }
        }
    }

    private fun changeAreaState() {
        binding.icAreaStatus.setImageResource(
            when (currentState) {
                State.UNKNOWN -> {
                    currentState = State.SAFE
                    R.drawable.ic_safe
                }
                State.SAFE -> {
                    currentState = State.UNSAFE
                    R.drawable.ic_unsafe
                }
                State.UNSAFE -> {
                    currentState = State.UNKNOWN
                    R.drawable.ic_unknown
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
