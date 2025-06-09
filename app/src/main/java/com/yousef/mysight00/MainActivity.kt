package com.yousef.mysight00

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.yousef.mysight00.databinding.ActivityMainBinding
import com.yousef.mysight00.model.UserType
import com.yousef.mysight00.utils.UserPreferences
import com.zegocloud.uikit.ZegoUIKit
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var userPreferences: UserPreferences
    private var userType: UserType? = null

    private val requestMicPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startZegoCallAudioOnly()
        } else {
            Toast.makeText(this, "الميكروفون مطلوب لإجراء المكالمات الصوتية", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        userType = determineUserType()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val navGraphRes = when {
            intent.getBooleanExtra("SKIP_SPLASH", false) -> R.navigation.auth_nav_graph
            userType == UserType.BLIND -> R.navigation.blind_nav_graph
            userType == UserType.ALZHEIMER -> R.navigation.alzheimer_nav_graph
            userType == UserType.COMPANION -> R.navigation.companion_nav_graph
            else -> R.navigation.auth_nav_graph
        }
        navController.setGraph(navGraphRes)

        if (intent.getBooleanExtra("SKIP_SPLASH", false)) {
            navController.navigate(R.id.loginFragment)
        }

        setupBottomNavigation()
        setupUI()
        hideSystemUI()
    }

    private fun determineUserType(): UserType? {
        val accountType = userPreferences.getUserType()?.lowercase()
        val name = userPreferences.getUserName()?.lowercase()

        return when {
            accountType == "companions" -> UserType.COMPANION
            accountType == "patients" && name == "blind" -> UserType.BLIND
            accountType == "patients" && name == "alzheimer" -> UserType.ALZHEIMER
            else -> null
        }
    }

    private fun setupUI() {
        val isBlind = userType == UserType.BLIND
        val isCompanion = userType == UserType.COMPANION

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // إظهار/إخفاء BottomNavigation حسب الصفحة والنوع
            val showBottomNav = when (userType) {
                UserType.BLIND -> destination.id in listOf(R.id.homeBlindFragment, R.id.gpsBlindFragment) && destination.id != R.id.callsFragment
                UserType.ALZHEIMER -> destination.id in listOf(R.id.homeAlzheimerFragment, R.id.gpsAlzheimerFragment) && destination.id != R.id.callsFragment
                UserType.COMPANION -> destination.id in listOf(R.id.homeCompanion, R.id.gpsCompanion) && destination.id != R.id.callsFragment
                else -> false
            }
            binding.bottomBarContainer.visibility = if (showBottomNav) View.VISIBLE else View.GONE

            // تحديث العنصر المحدد في BottomNavigation تلقائياً
            when (userType) {
                UserType.BLIND -> {
                    when (destination.id) {
                        R.id.homeBlindFragment -> binding.bottomNavigation.menu.findItem(R.id.homeIcon).isChecked = true
                        R.id.gpsBlindFragment -> binding.bottomNavigation.menu.findItem(R.id.gpsIcon).isChecked = true
                    }
                }
                UserType.ALZHEIMER -> {
                    when (destination.id) {
                        R.id.homeAlzheimerFragment -> binding.bottomNavigation.menu.findItem(R.id.homeIcon).isChecked = true
                        R.id.gpsAlzheimerFragment -> binding.bottomNavigation.menu.findItem(R.id.gpsIcon).isChecked = true
                    }
                }
                UserType.COMPANION -> {
                    when (destination.id) {
                        R.id.homeCompanion -> binding.bottomNavigation.menu.findItem(R.id.homeIcon).isChecked = true
                        R.id.gpsCompanion -> binding.bottomNavigation.menu.findItem(R.id.gpsIcon).isChecked = true
                    }
                }
                else -> {}
            }
        }

        binding.fabSos.visibility = if (isBlind || isCompanion) View.GONE else View.VISIBLE
        binding.fabSos.isEnabled = !isBlind && !isCompanion
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.menu.clear()

        when (userType) {
            UserType.BLIND -> {
                binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu_blind)
                binding.fabSos.visibility = View.VISIBLE
                binding.bottomNavigation.setOnItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.homeIcon -> {
                            navController.navigate(R.id.homeBlindFragment)
                            true
                        }
                        R.id.gpsIcon -> {
                            navController.navigate(R.id.gpsBlindFragment)
                            true
                        }
                        R.id.callIcon -> {
                            checkAndRequestMicPermission()
                            false
                        }
                        else -> false
                    }
                }
            }
            UserType.ALZHEIMER -> {
                binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu_alzheimer)
                binding.bottomNavigation.setOnItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.homeIcon -> {
                            navController.navigate(R.id.homeAlzheimerFragment)
                            true
                        }
                        R.id.gpsIcon -> {
                            navController.navigate(R.id.gpsAlzheimerFragment)
                            true
                        }
                        R.id.tasksIcon -> {
                            navController.navigate(R.id.tasksAlzheimerFragment)
                            true
                        }
                        R.id.callIcon -> {
                            checkAndRequestMicPermission()
                            false
                        }
                        else -> false
                    }
                }
            }
            UserType.COMPANION -> {
                binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu_companion)
                binding.bottomNavigation.setOnItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.homeIcon -> {
                            navController.navigate(R.id.homeCompanion)
                            true
                        }
                        R.id.gpsIcon -> {
                            navController.navigate(R.id.gpsCompanion)
                            true
                        }
                        R.id.callIcon -> {
                            checkAndRequestMicPermission()
                            false
                        }
                        R.id.historyIcon -> {
                            navController.navigate(R.id.historyCompanion)
                            false
                        }
                        else -> false
                    }
                }
            }
            else -> {
                binding.bottomBarContainer.visibility = View.GONE
            }
        }
    }

    private fun checkAndRequestMicPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startZegoCallAudioOnly()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Toast.makeText(this, "يرجى منح الإذن بالميكروفون لإجراء المكالمات الصوتية", Toast.LENGTH_LONG).show()
                requestMicPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            else -> {
                requestMicPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startZegoCallAudioOnly() {
        val userName = userPreferences.getUserName() ?: "user"
        val userId = userPreferences.getUserId() ?: "0"

        val targetUserId = when (userType) {
            UserType.COMPANION -> userPreferences.getPatientId() ?: run {
                Toast.makeText(this, "لم يتم العثور على معرف المريض", Toast.LENGTH_LONG).show()
                return
            }
            UserType.BLIND, UserType.ALZHEIMER -> userPreferences.getCompanionId() ?: run {
                Toast.makeText(this, "لم يتم العثور على معرف المرافق", Toast.LENGTH_LONG).show()
                return
            }
            else -> {
                Toast.makeText(this, "نوع المستخدم غير صالح", Toast.LENGTH_LONG).show()
                return
            }
        }
        if (userId == targetUserId) {
            Toast.makeText(this, "لا يمكن الاتصال بنفسك", Toast.LENGTH_LONG).show()
            return
        }
        try {
            val callConfig = ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall()

            val callFragment = ZegoUIKitPrebuiltCallFragment.newInstance(
                constant.appId,
                constant.AppSign,
                userId,
                userName,
                targetUserId,
                callConfig
            )

            // الاستماع لانتهاء المكالمة
            ZegoUIKit.addRoomStateChangedListener { _, reason, _, _ ->
                if (reason == ZegoRoomStateChangedReason.LOGOUT ||
                    reason == ZegoRoomStateChangedReason.KICK_OUT ||
                    reason == ZegoRoomStateChangedReason.RECONNECT_FAILED
                ) {
                    runOnUiThread {
                        supportFragmentManager.popBackStack()
                        setBottomNavigationVisibility(true)
                    }
                }
            }

            // إخفاء الـ BottomNavigation قبل بدء المكالمة
            setBottomNavigationVisibility(false)

            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, callFragment)
                .addToBackStack(null)
                .commit()

            Toast.makeText(this, "جاري بدء المكالمة...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "حدث خطأ أثناء بدء المكالمة: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

    fun setBottomNavigationVisibility(isVisible: Boolean) {
        binding.bottomBarContainer.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}
