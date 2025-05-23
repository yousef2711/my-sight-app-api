package com.yousef.mysight00.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yousef.mysight00.R
import com.yousef.mysight00.databinding.FragmentGpsBlindBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class GpsBlindFragment : Fragment() {

    private var _binding: FragmentGpsBlindBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val safeZones = listOf(
        GeoPoint(30.0480, 31.2400),  // نقطة آمنة 1
        GeoPoint(30.0500, 31.2430)   // نقطة آمنة 2
    )

    private val dangerZones = listOf(
        GeoPoint(30.0460, 31.2320)   // نقطة خطر
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGpsBlindBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        setupClickListeners()
        val map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(17.0)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userLocation = GeoPoint(location.latitude, location.longitude)
                map.controller.setCenter(userLocation)
                addMarker(userLocation, "You are here", R.drawable.ic_patient_location)

                drawZones()

                if (isInDangerZone(userLocation)) {
                    Toast.makeText(requireContext(), "⚠️Warning: You are in a dangerous area!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.logoProfileHomeComp.setOnClickListener {
            findNavController().navigate(R.id.action_gps_to_profile)
        }
    }

    private fun drawZones() {
        val map = binding.map
        safeZones.forEach {
            addMarker(it, "SAFE AREA", R.drawable.ic_safe)
        }
        dangerZones.forEach {
            addMarker(it, "DANGER AREA", R.drawable.ic_unsafe)
        }
    }

    private fun addMarker(point: GeoPoint, title: String, iconRes: Int) {
        val marker = Marker(binding.map)
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title
        marker.icon = resources.getDrawable(iconRes, null)
        binding.map.overlays.add(marker)
    }

    private fun isInDangerZone(userPoint: GeoPoint): Boolean {
        return dangerZones.any {
            userPoint.distanceToAsDouble(it) < 100  // أقل من 100 متر تعتبر منطقة خطر
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
