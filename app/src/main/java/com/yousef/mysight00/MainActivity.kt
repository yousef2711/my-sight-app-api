package com.yousef.mysight00

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.yousef.mysight00.databinding.ActivityMainBinding
import com.yousef.mysight00.model.UserType
import com.yousef.mysight00.utils.UserPreferences
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var userType: UserType? = null
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        userPreferences = UserPreferences(this)

        // مسح بيانات المستخدم في كل مرة يتم فيها فتح التطبيق (يمكنك تعديل هذا حسب الحاجة)
        userPreferences.clearUserData()

        setContentView(binding.root)

        // تهيئة Navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // تعيين الرسم البياني للتنقل الافتراضي (مثلاً شاشة المصادقة)
        val navInflater = navController.navInflater
        val graph = navInflater.inflate(R.navigation.auth_nav_graph)
        navController.graph = graph

        // الانتقال إلى شاشة Splash
        navController.navigate(R.id.splashFragment)

        hideSystemUI()
    }

    // هذه الدالة تستخدم بعد تحديد userType (مثلاً بعد تسجيل الدخول) لتهيئة التنقل والواجهة
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.menu.clear()

        val navInflater = navController.navInflater
        val graph: NavGraph = when (userType) {
            UserType.COMPANION -> {
                binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu_companion)
                navInflater.inflate(R.navigation.companion_nav_graph)
            }
            UserType.BLIND -> {
                binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu_blind)
                navInflater.inflate(R.navigation.blind_nav_graph)
            }
            UserType.ALZHEIMER -> {
                binding.bottomNavigation.inflateMenu(R.menu.bottom_nav_menu_alzheimer)
                navInflater.inflate(R.navigation.alzhaimer_nav_graph)
            }
            else -> navInflater.inflate(R.navigation.auth_nav_graph)
        }
        navController.graph = graph

        // إظهار أو إخفاء القائمة السفلية بناءً على الشاشة الحالية
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val bottomNavDestinations = when (userType) {
                UserType.COMPANION -> setOf(R.id.homeCompanion, R.id.gpsCompanion, R.id.historyCompanion)
                UserType.BLIND -> setOf(R.id.homeBlindFragment, R.id.gpsBlindFragment)
                UserType.ALZHEIMER -> setOf(R.id.homeAlzheimerFragment, R.id.gpsAlzheimerFragment)
                else -> emptySet()
            }

            if (bottomNavDestinations.contains(destination.id)) {
                binding.bottomBarContainer.visibility = View.VISIBLE
                binding.fabSos.visibility = if (userType == UserType.BLIND) View.GONE else View.VISIBLE
                binding.bottomNavigation.menu.findItem(destination.id)?.isChecked = true
            } else {
                binding.bottomBarContainer.visibility = View.GONE
                binding.fabSos.visibility = View.GONE
                for (i in 0 until binding.bottomNavigation.menu.size()) {
                    binding.bottomNavigation.menu.getItem(i).isChecked = false
                }
            }
        }

        // التعامل مع الضغط على عناصر القائمة السفلية حسب نوع المستخدم
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (userType) {
                UserType.COMPANION -> handleCompanionNavigation(item.itemId)
                UserType.BLIND -> handleBlindNavigation(item.itemId)
                UserType.ALZHEIMER -> handleAlzheimerNavigation(item.itemId)
                else -> false
            }
        }
    }

    private fun handleCompanionNavigation(itemId: Int): Boolean {
        return when (itemId) {
            R.id.homeCompanion -> {
                navController.navigate(R.id.homeCompanion)
                true
            }
            R.id.gpsCompanion -> {
                navController.navigate(R.id.gpsCompanion)
                true
            }
            R.id.historyCompanion -> {
                navController.navigate(R.id.historyCompanion)
                true
            }
            R.id.callsFragment -> {
                startAudioCall()
                true
            }
            else -> false
        }
    }

    private fun handleBlindNavigation(itemId: Int): Boolean {
        return when (itemId) {
            R.id.homeBlindFragment -> {
                navController.navigate(R.id.homeBlindFragment)
                true
            }
            R.id.gpsBlindFragment -> {
                navController.navigate(R.id.gpsBlindFragment)
                true
            }
            R.id.callsFragment -> {
                startAudioCall()
                true
            }
            else -> false
        }
    }

    private fun handleAlzheimerNavigation(itemId: Int): Boolean {
        return when (itemId) {
            R.id.homeAlzheimerFragment -> {
                navController.navigate(R.id.homeAlzheimerFragment)
                true
            }
            R.id.gpsAlzheimerFragment -> {
                navController.navigate(R.id.gpsAlzheimerFragment)
                true
            }
            R.id.tasksAlzheimerFragment -> {
                navController.navigate(R.id.tasksAlzheimerFragment)
                true
            }
            R.id.callsFragment -> {
                startAudioCall()
                true
            }
            else -> false
        }
    }

    private fun startAudioCall() {
        val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
        ZegoUIKitPrebuiltCallService.init(
            application,
            constant.appId,        // ضع هنا الـ appId من ZEGOCLOUD
            constant.AppSign,      // ضع هنا الـ appSign من ZEGOCLOUD
            userPreferences.getUserId() ?: "UnknownID",
            if (userType == UserType.COMPANION) {
                userPreferences.getPatientId() ?: "UnknownPatientID"
            } else {
                "Josef" // اسم المرافق الثابت للمريض - عدل حسب حالتك
            },
            callInvitationConfig
        )
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }
}
