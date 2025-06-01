package com.yousef.mysight00

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.yousef.mysight00.databinding.ActivityMainBinding
import com.yousef.mysight00.model.UserType
import com.yousef.mysight00.utils.UserPreferences
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var userPreferences: UserPreferences
    private var userType: UserType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        userType = determineUserType()

        // Initialize navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up navigation graph based on user type
        val navGraphRes = when {
            intent.getBooleanExtra("SKIP_SPLASH", false) -> R.navigation.auth_nav_graph
            userType == UserType.BLIND -> R.navigation.blind_nav_graph
            userType == UserType.ALZHEIMER -> R.navigation.alzheimer_nav_graph
            userType == UserType.COMPANION -> R.navigation.companion_nav_graph
            else -> R.navigation.auth_nav_graph
        }
        navController.setGraph(navGraphRes)

        // إذا كان يجب تخطي شاشة السبلاش، انتقل مباشرة إلى شاشة تسجيل الدخول
        if (intent.getBooleanExtra("SKIP_SPLASH", false)) {
            navController.navigate(R.id.loginFragment)
        }

        // Set up bottom navigation and UI based on user type
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
        // Show/hide bottom navigation based on user type
        binding.bottomBarContainer.visibility = if (userType != null) View.VISIBLE else View.GONE
        binding.fabSos.visibility = if (userType != null) View.VISIBLE else View.GONE

        // Set up SOS button click listener
        binding.fabSos.setOnClickListener {
            startAudioCall()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.menu.clear()  // مهم جدا لمسح القوائم السابقة

        when (userType) {
            UserType.BLIND -> {
                binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu_blind)
                binding.bottomNavigation.setOnItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.homeIcon -> navController.navigate(R.id.homeBlindFragment)
                        R.id.gpsIcon -> navController.navigate(R.id.gpsBlindFragment)
                        R.id.callIcon -> startAudioCall()
                    }
                    true
                }
            }
            UserType.ALZHEIMER -> {
                binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu_alzheimer)
                binding.bottomNavigation.setOnItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.homeIcon -> navController.navigate(R.id.homeAlzheimerFragment)
                        R.id.gpsIcon -> navController.navigate(R.id.gpsAlzheimerFragment)
                        R.id.tasksIcon -> navController.navigate(R.id.tasksAlzheimerFragment)
                        R.id.callIcon -> startAudioCall()
                    }
                    true
                }
            }
            UserType.COMPANION -> {
                binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu_companion)
                binding.bottomNavigation.setOnItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.homeIcon -> navController.navigate(R.id.homeCompanion)
                        R.id.gpsIcon -> navController.navigate(R.id.gpsCompanion)
                        R.id.historyIcon -> navController.navigate(R.id.historyCompanion)
                        R.id.callIcon -> startAudioCall()
                    }
                    true
                }
            }
            else -> {
                binding.bottomNavigation.setOnItemSelectedListener(null)
            }
        }

        // تحديث الرؤية بناءً على وجهة التنقل الحالية
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val showBottomNav = when (userType) {
                UserType.BLIND -> destination.id in listOf(R.id.homeBlindFragment, R.id.gpsBlindFragment)
                UserType.ALZHEIMER -> destination.id in listOf(R.id.homeAlzheimerFragment, R.id.gpsAlzheimerFragment, R.id.tasksAlzheimerFragment)
                UserType.COMPANION -> destination.id in listOf(R.id.homeCompanion, R.id.gpsCompanion, R.id.historyCompanion)
                else -> false
            }

            binding.bottomBarContainer.visibility = if (showBottomNav) View.VISIBLE else View.GONE
            binding.fabSos.visibility = if (showBottomNav) View.VISIBLE else View.GONE
        }
    }

    private fun startAudioCall() {
        val userId = userPreferences.getUserId() ?: "UnknownUser"
        val calleeId = when (userType) {
            UserType.COMPANION -> userPreferences.getPatientName() ?: "UnknownPatient"
            UserType.BLIND, UserType.ALZHEIMER -> userPreferences.getCompanionName() ?: "UnknownCompanion"
            else -> "UnknownCallee"
        }

        val config = ZegoUIKitPrebuiltCallInvitationConfig()

        ZegoUIKitPrebuiltCallService.init(
            application,
            constant.appId,
            constant.AppSign,
            userId,
            calleeId,
            config
        )
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }
}
