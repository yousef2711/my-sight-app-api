package com.yousef.mysight00.ui.Gps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
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
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class GpsBlindFragment : BaseFragment() {

    private var _binding: FragmentGpsBlindBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationOverlay: MyLocationNewOverlay

    private var lastLocation: GeoPoint? = null

    private val safeZones = listOf(
        GeoPoint(30.0480, 31.2400),
        GeoPoint(30.0500, 31.2430)
    )

    private val dangerZones = listOf(
        GeoPoint(30.0460, 31.2320)
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

        val ctx = requireContext()

        val config = Configuration.getInstance()
        config.load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        config.osmdroidBasePath = ctx.cacheDir
        config.osmdroidTileCache = ctx.cacheDir
        config.setUserAgentValue(ctx.packageName)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)

        setupClickListeners()
        setupMap()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                Log.d("GPS", "Current location: ${geoPoint.latitude}, ${geoPoint.longitude}")
                updateUserLocation(geoPoint)
            }
        }

        if (ActivityCompat.checkSelfPermission(
                ctx, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
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
        map.setTilesScaledToDpi(true)
        map.controller.setZoom(17.0)
        map.controller.setCenter(GeoPoint(30.0480, 31.2400))
        map.setBackgroundColor(Color.WHITE)

        drawZones()

        // إعداد MyLocation overlay لإظهار موقع المستخدم
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), map)
        locationOverlay.enableMyLocation()
        map.overlays.add(locationOverlay)
    }

    private fun drawZones() {
        val map = binding.map

        // امسح فقط دوائر المناطق (Polygon) القديمة، لا تمسح علامات المستخدم
        val polygonsToRemove = map.overlays.filterIsInstance<Polygon>()
        map.overlays.removeAll(polygonsToRemove)

        safeZones.forEach { center ->
            addCircle(center, 100.0, Color.argb(60, 0, 255, 0))
        }

        dangerZones.forEach { center ->
            addCircle(center, 100.0, Color.argb(60, 255, 0, 0))
        }

        map.invalidate()
    }

    private fun addCircle(center: GeoPoint, radiusMeters: Double, fillColor: Int) {
        val circle = Polygon(binding.map).apply {
            setPoints(createCirclePoints(center, radiusMeters))
            fillPaint.color = fillColor
            fillPaint.style = android.graphics.Paint.Style.FILL_AND_STROKE
            outlinePaint.color = Color.TRANSPARENT
        }
        binding.map.overlays.add(circle)
    }

    private fun createCirclePoints(center: GeoPoint, radiusMeters: Double, pointsCount: Int = 36): List<GeoPoint> {
        val earthRadius = 6371000.0
        val lat = Math.toRadians(center.latitude)
        val lon = Math.toRadians(center.longitude)
        val d = radiusMeters / earthRadius

        val circlePoints = mutableListOf<GeoPoint>()
        for (i in 0 until pointsCount) {
            val bearing = Math.toRadians(i * (360.0 / pointsCount))
            val latRadians = Math.asin(
                Math.sin(lat) * Math.cos(d) +
                        Math.cos(lat) * Math.sin(d) * Math.cos(bearing)
            )
            val lonRadians = lon + Math.atan2(
                Math.sin(bearing) * Math.sin(d) * Math.cos(lat),
                Math.cos(d) - Math.sin(lat) * Math.sin(latRadians)
            )
            circlePoints.add(GeoPoint(Math.toDegrees(latRadians), Math.toDegrees(lonRadians)))
        }
        circlePoints.add(circlePoints[0])
        return circlePoints
    }

    private fun updateUserLocation(userPoint: GeoPoint) {
        if (lastLocation?.distanceToAsDouble(userPoint) ?: Double.MAX_VALUE < 2.0) return
        lastLocation = userPoint

        val map = binding.map
        map.controller.animateTo(userPoint)
        map.invalidate()

        if (isInDangerZone(userPoint)) {
            Toast.makeText(requireContext(), "⚠️ تحذير: أنت في منطقة خطر!", Toast.LENGTH_LONG).show()
        }
    }

    private fun isInDangerZone(userPoint: GeoPoint): Boolean {
        return dangerZones.any {
            userPoint.distanceToAsDouble(it) < 100
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(requireContext(), "تم رفض صلاحية الوصول للموقع", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _binding = null
    }
}
