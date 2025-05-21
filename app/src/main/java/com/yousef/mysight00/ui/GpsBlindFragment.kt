package com.yousef.mysight00.ui

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yousef.mysight00.R
import com.yousef.mysight00.databinding.FragmentGpsBlindBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint

class GpsBlindFragment : Fragment() {

    private var _binding: FragmentGpsBlindBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGpsBlindBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupMap()
    }

    private fun setupClickListeners() {
        binding.logoProfileHomeComp.setOnClickListener {
            findNavController().navigate(R.id.action_gps_to_profile)
        }
    }

    private fun setupMap() {
        Configuration.getInstance()
            .load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))

        val map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)

        val mapController = map.controller
        mapController.setZoom(15.0)

        val startPoint = GeoPoint(30.0444, 31.2357) // Cairo, Egypt as default center
        mapController.setCenter(startPoint)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
