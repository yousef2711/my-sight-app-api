package com.yousef.mysight00.utils

import android.content.Context
import android.content.Intent
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity

class UserPreferences(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("MySightPrefs", AppCompatActivity.MODE_PRIVATE)

    companion object {
        private const val USER_TYPE = "USER_TYPE"
        private const val PATIENT_ID = "PATIENT_ID"
        private const val USER_ID = "USER_ID"
    }

    fun saveUserType(userType: String) {
        sharedPreferences.edit().putString(USER_TYPE, userType).apply()
    }

    fun getUserType(): String? {
        return sharedPreferences.getString(USER_TYPE, null)
    }

    fun savePatientId(patientId: String) {
        sharedPreferences.edit().putString(PATIENT_ID, patientId).apply()
    }

    fun getPatientId(): String? {
        return sharedPreferences.getString(PATIENT_ID, null)
    }

    fun saveUserId(userId: String) {
        sharedPreferences.edit().putString(USER_ID, userId).apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString(USER_ID, null)
    }

    fun clearUserData() {
        sharedPreferences.edit().clear().apply()
    }

    fun logout(context: Context) {
        clearUserData()
        // إعادة تشغيل التطبيق
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        if (context is Activity) {
            context.finish()
        }
    }
}