package com.yousef.mysight00.ui.Gps

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
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.yousef.mysight00.R
import com.yousef.mysight00.databinding.FragmentGpsAlzheimerBinding
import com.yousef.mysight00.ui.base.BaseFragment
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class GpsAlzheimerFragment : BaseFragment() {

    private var _binding: FragmentGpsAlzheimerBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private enum class State {
        UNKNOWN, SAFE, UNSAFE
    }

    private var currentState = State.UNKNOWN

    private val safeZones = listOf(
        GeoPoint(30.0450, 31.2360),  // مثال منطقة آمنة
        GeoPoint(30.0448, 31.2355)
    )
    private val unsafeZones = listOf(
        GeoPoint(30.0430, 31.2340),  // مثال منطقة غير آمنة
        GeoPoint(30.0465, 31.2375)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGpsAlzheimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupClickListeners()
        setupMap()
        startLocationUpdates()
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

    private fun setupMap() {
        Configuration.getInstance()
            .load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))

        binding.map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(true)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(3000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                updateMapWithLocation(location)
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun updateMapWithLocation(location: Location) {
        val userPoint = GeoPoint(location.latitude, location.longitude)
        val map = binding.map

        map.controller.setZoom(17.0)
        map.controller.setCenter(userPoint)

        map.overlays.clear()  // نمسح الماركرز القديمة قبل إضافة الجديد

        // إضافة Marker لموقع المريض الحالي
        val marker = Marker(map)
        marker.position = userPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = resources.getDrawable(R.drawable.ic_patient_location, null) // عدل حسب أيقونتك
        marker.title = "موقعك الحالي"
        map.overlays.add(marker)

        map.invalidate()

        updateAreaStatus(userPoint)
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

    private fun isInZone(
        current: GeoPoint,
        zone: List<GeoPoint>,
        radius: Double = 0.0008
    ): Boolean {
        return zone.any {
            val distance = it.distanceToAsDouble(current)
            distance <= radius * 111000 // تقريبًا تحويل درجة إلى متر
        }
    }

    private fun goToNearestSafeZone() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

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
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _binding = null
    }
}
