package com.yousef.mysight00.utils

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("my_sight_prefs", Context.MODE_PRIVATE)

    // Token Management
    fun saveAccessToken(token: String) {
        sharedPreferences.edit().putString("access_token", token).apply()
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }

    fun saveRefreshToken(token: String) {
        sharedPreferences.edit().putString("refresh_token", token).apply()
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    fun saveUserName(name: String) {
        sharedPreferences.edit().putString("user_name", name).apply()
    }

    fun getUserName(): String? {
        return sharedPreferences.getString("user_name", null)
    }

    fun saveUsername(username: String) {
        sharedPreferences.edit().putString("username", username).apply()
    }

    fun getUsername(): String? {
        return sharedPreferences.getString("username", null)
    }

    fun saveUserEmail(email: String) {
        sharedPreferences.edit().putString("user_email", email).apply()
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString("user_email", null)
    }

    fun saveUserPhone(phone: String) {
        sharedPreferences.edit().putString("user_phone", phone).apply()
    }

    fun getUserPhone(): String? {
        return sharedPreferences.getString("user_phone", null)
    }

    fun saveUserLocation(location: String) {
        sharedPreferences.edit().putString("user_location", location).apply()
    }

    fun getUserLocation(): String? {
        return sharedPreferences.getString("user_location", null)
    }

    fun saveUserRelationship(relationship: String) {
        sharedPreferences.edit().putString("user_relationship", relationship).apply()
    }

    fun getUserRelationship(): String? {
        return sharedPreferences.getString("user_relationship", null)
    }

    fun savePatientName(patientName: String) {
        sharedPreferences.edit().putString("patient_name", patientName).apply()
    }

    fun getPatientName(): String? {
        return sharedPreferences.getString("patient_name", null)
    }

    fun savePatientType(patientType: String) {
        sharedPreferences.edit().putString("patient_type", patientType).apply()
    }

    fun getPatientType(): String? {
        return sharedPreferences.getString("patient_type", null)
    }

    fun saveCompanionName(linkedCompanionName: String) {
        sharedPreferences.edit().putString("companion_name", linkedCompanionName).apply()
    }

    fun getCompanionName(): String? {
        return sharedPreferences.getString("companion_name", null)
    }

    fun saveUserType(userType: String) {
        sharedPreferences.edit().putString("user_type", userType).apply()
    }

    fun getUserType(): String? {
        return sharedPreferences.getString("user_type", null)
    }

    fun saveUserId(userId: String) {
        sharedPreferences.edit().putString("user_id", userId).apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString("user_id", null)
    }

    fun saveUserAvatar(avatarResId: Int) {
        sharedPreferences.edit().putInt("user_avatar", avatarResId).apply()
    }

    fun getUserAvatar(): Int? {
        val avatarResId = sharedPreferences.getInt("user_avatar", -1)
        return if (avatarResId != -1) avatarResId else null
    }

    fun savePatientId(patientId: String) {
        sharedPreferences.edit().putString("patient_id", patientId).apply()
    }

    fun getPatientId(): String? {
        return sharedPreferences.getString("patient_id", null)
    }

    fun saveCompanionId(companionId: String) {
        sharedPreferences.edit().putString("companion_id", companionId).apply()
    }

    fun getCompanionId(): String? {
        return sharedPreferences.getString("companion_id", null)
    }

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}
