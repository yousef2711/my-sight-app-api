package com.yousef.mysight00.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yousef.mysight00.R
import com.yousef.mysight00.databinding.FragmentGpsAlzheimerBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class GpsAlzheimerFragment : Fragment() {

    private var _binding: FragmentGpsAlzheimerBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private enum class State {
        UNKNOWN, SAFE, UNSAFE
    }

    private var currentState = State.UNKNOWN

    private val safeZones = listOf(
        GeoPoint(30.0450, 31.2360),
        GeoPoint(30.0448, 31.2355)
    )
    private val unsafeZones = listOf(
        GeoPoint(30.0430, 31.2340),
        GeoPoint(30.0465, 31.2375)
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGpsAlzheimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupClickListeners()
        setupMap()
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
                goToNearestSafeZone()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupMap() {
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        val map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.setBuiltInZoomControls(true)

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userPoint = GeoPoint(it.latitude, it.longitude)
                binding.map.controller.setZoom(17.0)
                binding.map.controller.setCenter(userPoint)

                // ✳️ إضافة Marker لمكان المريض
                val marker = Marker(binding.map)
                marker.position = userPoint
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.icon = resources.getDrawable(R.drawable.ic_patient_location, null) // ← حط الأيقونة المناسبة هنا
                marker.title = "موقعك الحالي"
                binding.map.overlays.add(marker)
                binding.map.invalidate()

                updateAreaStatus(userPoint)
            }
        }
    }

    private fun updateAreaStatus(currentLocation: GeoPoint) {
        currentState = when {
            isInZone(currentLocation, safeZones) -> State.SAFE
            isInZone(currentLocation, unsafeZones) -> State.UNSAFE
            else -> State.UNKNOWN
        }

        binding.icAreaStatus.setImageResource(
            when (currentState) {
                State.SAFE -> R.drawable.ic_safe
                State.UNSAFE -> R.drawable.ic_unsafe
                State.UNKNOWN -> R.drawable.ic_unknown
            }
        )
    }

    private fun isInZone(current: GeoPoint, zone: List<GeoPoint>, radius: Double = 0.0008): Boolean {
        return zone.any {
            val distance = it.distanceToAsDouble(current)
            distance <= radius * 111000 // تحويل من درجات إلى أمتار تقريبًا
        }
    }

    private fun goToNearestSafeZone() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userPoint = GeoPoint(it.latitude, it.longitude)
                val nearest = safeZones.minByOrNull { zone -> zone.distanceToAsDouble(userPoint) }
                nearest?.let {
                    binding.map.controller.animateTo(it)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
