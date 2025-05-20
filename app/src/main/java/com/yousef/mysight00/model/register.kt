package com.yousef.mysight00.model

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val phone_number: String,
    val name: String,
    val account_type: String,
    val patient_username: String? = null,
    val relationship: String? = null
)

data class RegisterResponse(
    val success: Boolean,
    val message: String? = null,
    val error: Errors? = null
)

data class Errors(
    val username: List<String>? = null,
    val email: List<String>? = null,
    val phone_number: List<String>? = null
)

enum class UserType(val accountType: String) {
    BLIND("patients"),
    ALZHEIMER("patients"),
    COMPANION("companions")
}
