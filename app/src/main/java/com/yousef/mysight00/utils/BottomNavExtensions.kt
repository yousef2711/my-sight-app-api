package com.yousef.mysight00.utils

import android.content.Context
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yousef.mysight00.R

/**
 * Extension function to setup bottom navigation with default styling
 */
fun BottomNavigationView.setupBottomNavigation(context: Context) {
    // تطبيق الألوان الافتراضية
    itemIconTintList = createBottomNavColors(context)
    itemTextColor = createBottomNavColors(context)
    
    // تطبيق الخلفية
    setBackgroundColor(ContextCompat.getColor(context, R.color.dark))
    
    // تطبيق الارتفاع والحجم
    elevation = 10f
}

/**
 * Creates color state list for bottom navigation items
 */
private fun createBottomNavColors(context: Context): ColorStateList {
    return ColorStateList(
        arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        ),
        intArrayOf(
            ContextCompat.getColor(context, R.color.white), // Selected color
            ContextCompat.getColor(context, R.color.white) // Unselected color
        )
    )
}

/**
 * Extension function to handle bottom navigation item selection
 */
fun BottomNavigationView.onNavItemSelected(
    onHomeSelected: () -> Unit = {},
    onCallsSelected: () -> Unit = {},
    onGpsSelected: () -> Unit = {},
    onHistorySelected: () -> Unit = {}
) {
    setOnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                onHomeSelected()
                true
            }
            R.id.nav_calls -> {
                onCallsSelected()
                true
            }
            R.id.nav_gps -> {
                onGpsSelected()
                true
            }
            R.id.nav_history -> {
                onHistorySelected()
                true
            }
            else -> false
        }
    }
}

/**
 * Extension function to set the selected item
 */
fun BottomNavigationView.setSelectedItem(itemId: Int) {
    selectedItemId = itemId
} 