package com.yousef.mysight00.ui.Gps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.yousef.mysight00.R
import com.yousef.mysight00.databinding.FragmentGpsBlindBinding
import com.yousef.mysight00.ui.base.BaseFragment
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class GpsBlindFragment : BaseFragment() {

    private var _binding: FragmentGpsBlindBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var userMarker: Marker? = null

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
        setupMap()

        // إعداد LocationCallback لتحديث الموقع باستمرار
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                updateUserLocation(GeoPoint(location.latitude, location.longitude))
            }
        }

        // التحقق من صلاحية الوصول للموقع وبدء التحديثات
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }
    }

    private fun setupClickListeners() {
        binding.logoProfileHomeComp.setOnClickListener {
            findNavController().navigate(R.id.action_gps_to_profile)
        }
    }

    private fun setupMap() {
        val map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.setBuiltInZoomControls(true)
        map.controller.setZoom(17.0)
        drawZones()
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

    private fun updateUserLocation(userPoint: GeoPoint) {
        val map = binding.map

        // تحديث علامة المستخدم أو إضافتها لو مش موجودة
        if (userMarker == null) {
            userMarker = Marker(map).apply {
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "موقعك الحالي"
                icon = resources.getDrawable(R.drawable.ic_patient_location, null)
                map.overlays.add(this)
            }
        }
        userMarker?.position = userPoint

        // تحريك الكاميرا إلى الموقع الجديد
        map.controller.animateTo(userPoint)

        map.invalidate()

        // تحقق هل المستخدم في منطقة خطر
        if (isInDangerZone(userPoint)) {
            Toast.makeText(requireContext(), "⚠️ تحذير: أنت في منطقة خطر!", Toast.LENGTH_LONG).show()
        }
    }

    private fun isInDangerZone(userPoint: GeoPoint): Boolean {
        return dangerZones.any {
            userPoint.distanceToAsDouble(it) < 100  // أقل من 100 متر تعتبر منطقة خطر
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setMinUpdateIntervalMillis(1000)
            .setMaxUpdateDelayMillis(3000)
            .build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _binding = null
    }
}
