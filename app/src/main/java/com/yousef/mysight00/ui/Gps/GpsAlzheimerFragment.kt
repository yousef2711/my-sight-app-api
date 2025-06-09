package com.yousef.mysight00.ui.Gps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
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
import org.osmdroid.views.overlay.Polygon

class GpsAlzheimerFragment : BaseFragment() {

    private var _binding: FragmentGpsAlzheimerBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var userMarker: Marker? = null

    private enum class State { UNKNOWN, SAFE, UNSAFE }
    private var currentState = State.UNKNOWN

    private val safeZones = listOf(
        GeoPoint(30.0480, 31.2400),
        GeoPoint(30.0500, 31.2430)
    )
    private val unsafeZones = listOf(
        GeoPoint(30.0460, 31.2320)
    )

    // ماركر مؤقت لعرض InfoWindow عند الضغط على الـ Polygon
    private var tempInfoMarker: Marker? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGpsAlzheimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ctx = requireContext().applicationContext
        val config = Configuration.getInstance()
        config.load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        config.userAgentValue = ctx.packageName
        config.osmdroidBasePath = ctx.cacheDir
        config.osmdroidTileCache = ctx.cacheDir

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupClickListeners()
        setupMap()
        checkPermissionsAndStartLocationUpdates()
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
        val map = binding.map
        map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(true)
            setTilesScaledToDpi(true)
            controller.setZoom(17.0)
            controller.setCenter(safeZones[0])
        }

        drawZones()
        // وضع ماركر مبدئي لموقع المستخدم (اختياري)
        updateMapWithGeoPoint(safeZones[0])
    }

    private fun drawZones() {
        val map = binding.map

        // إزالة جميع الدوائر فقط (Polygon) من الخريطة، لا تمسح الماركرات
        map.overlays.removeAll { it is Polygon }

        safeZones.forEach {
            addCircle(it, 100.0, Color.argb(80, 0, 255, 0), "منطقة آمنة")
        }
        unsafeZones.forEach {
            addCircle(it, 100.0, Color.argb(80, 255, 0, 0), "منطقة غير آمنة")
        }

        map.invalidate()
    }

    // دالة ترسم دائرة (Polygon) مع خاصية الضغط عليها
    private fun addCircle(center: GeoPoint, radiusMeters: Double, fillColor: Int, title: String) {
        val circle = Polygon(binding.map).apply {
            setPoints(createCirclePoints(center, radiusMeters))
            fillPaint.color = fillColor
            fillPaint.style = android.graphics.Paint.Style.FILL_AND_STROKE
            outlinePaint.color = Color.TRANSPARENT

            setOnClickListener { _, _, _ ->
                showPolygonInfoWindow(center, title)
                true
            }
        }
        binding.map.overlays.add(circle)
    }

    // إنشاء نقاط الدائرة لتشكيل Polygon
    private fun createCirclePoints(center: GeoPoint, radiusMeters: Double, pointsCount: Int = 36): List<GeoPoint> {
        val earthRadius = 6371000.0
        val lat = Math.toRadians(center.latitude)
        val lon = Math.toRadians(center.longitude)
        val d = radiusMeters / earthRadius

        return (0 until pointsCount).map {
            val bearing = Math.toRadians(it * (360.0 / pointsCount))
            val latRadians = Math.asin(Math.sin(lat) * Math.cos(d) + Math.cos(lat) * Math.sin(d) * Math.cos(bearing))
            val lonRadians = lon + Math.atan2(Math.sin(bearing) * Math.sin(d) * Math.cos(lat), Math.cos(d) - Math.sin(lat) * Math.sin(latRadians))
            GeoPoint(Math.toDegrees(latRadians), Math.toDegrees(lonRadians))
        }
    }

    // عرض InfoWindow عند الضغط على الـ Polygon عن طريق Marker مؤقت
    private fun showPolygonInfoWindow(position: GeoPoint, title: String) {
        // إزالة الماركر المؤقت السابق إذا موجود
        tempInfoMarker?.let {
            binding.map.overlays.remove(it)
            tempInfoMarker = null
        }

        tempInfoMarker = Marker(binding.map).apply {
            this.position = position
            this.title = title
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = resources.getDrawable(R.drawable.ic_location, null)
            binding.map.overlays.add(this)

            showInfoWindow()

            // عند الضغط على الماركر نفسه، نغلق النافذة ونزيل الماركر
            setOnMarkerClickListener { marker, _ ->
                if (marker.infoWindow.isOpen) {
                    marker.closeInfoWindow()
                    binding.map.overlays.remove(marker)
                    tempInfoMarker = null
                    binding.map.invalidate()
                    true
                } else {
                    false
                }
            }
        }

        binding.map.invalidate()
    }



    private fun checkPermissionsAndStartLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            startLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(3000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                updateMapWithGeoPoint(GeoPoint(location.latitude, location.longitude))
            }
        }

        fusedLocationClient.requestLocationUpdates(request, locationCallback, null)
    }

    private fun updateMapWithGeoPoint(point: GeoPoint) {
        val map = binding.map
        map.controller.setCenter(point)

        if (userMarker == null) {
            userMarker = Marker(map).apply {
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = resources.getDrawable(R.drawable.ic_patient_location, null)
                title = "موقعك الحالي"
            }
            map.overlays.add(userMarker)
        }

        userMarker?.position = point
        drawZones()
        updateAreaStatus(point)
    }

    private fun updateAreaStatus(currentLocation: GeoPoint) {
        val previousState = currentState

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
        return zone.any { it.distanceToAsDouble(current) <= radius * 111000 }
    }

    private fun goToNearestSafeZone() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val current = GeoPoint(it.latitude, it.longitude)
                val nearest = safeZones.minByOrNull { zone -> zone.distanceToAsDouble(current) }
                nearest?.let { binding.map.controller.animateTo(it) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        _binding = null
    }
}
