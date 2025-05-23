package com.yousef.mysight00

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.yousef.mysight00.databinding.ActivityMainBinding
import com.yousef.mysight00.model.UserType

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var userType: UserType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userType = UserType.fromString(intent.getStringExtra("user_type"))
        if (userType == UserType.BLIND) {
            binding.fabSos.visibility = View.GONE
        }

        setupNavigation()
        hideSystemUI()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
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

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val bottomNavDestinations = when (userType) {
                UserType.COMPANION -> setOf(
                    R.id.homeCompanionFragment,
                    R.id.gpsCompanionFragment,
                    R.id.historyCompanionFragment
                )
                UserType.BLIND -> setOf(
                    R.id.homeBlindFragment,
                    R.id.gpsBlindFragment
                )
                UserType.ALZHEIMER -> setOf(
                    R.id.homeAlzheimerFragment,
                    R.id.gpsAlzheimerFragment
                )
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
            R.id.homeCompanionFragment -> {
                navController.navigate(R.id.homeCompanionFragment)
                true
            }
            R.id.gpsCompanionFragment -> {
                navController.navigate(R.id.gpsCompanionFragment)
                true
            }
            R.id.historyCompanionFragment -> {
                navController.navigate(R.id.historyCompanionFragment)
                true
            }
            R.id.audioCallCompanionFragment -> {
                navController.navigate(R.id.audioCallCompanionFragment)
                true
            }
            else -> false
        }
    }

    private fun handleBlindNavigation(itemId: Int): Boolean {
        return when (itemId) {
            R.id.navigation_home -> {
                navController.navigate(R.id.homeBlindFragment)
                true
            }
            R.id.navigation_gps -> {
                navController.navigate(R.id.gpsBlindFragment)
                true
            }
            else -> false
        }
    }

    private fun handleAlzheimerNavigation(itemId: Int): Boolean {
        return when (itemId) {
            R.id.navigation_home -> {
                navController.navigate(R.id.homeAlzheimerFragment)
                true
            }
            R.id.navigation_gps -> {
                navController.navigate(R.id.gpsAlzheimerFragment)
                true
            }
            R.id.navigation_task -> {
                navController.navigate(R.id.tasksAlzheimerFragment)
                true
            }
            R.id.navigation_call -> {
                navController.navigate(R.id.audioCallAlzheimerFragment)
                true
            }
            else -> false
        }
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
