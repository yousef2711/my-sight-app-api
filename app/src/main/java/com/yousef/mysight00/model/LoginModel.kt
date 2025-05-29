package com.yousef.mysight00.model

data class loginRequest(
    val username: String,
    val password: String
)

data class loginResponse(
    val user: User? = null,
    val detail: String? = null,
)


data class User(
    val id: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val account_type: String? = null,
    val phone_number: String? = null,
    val location: String? = null,
    val medical_condition: String? = null,
    val account_photo: String? = null,
    val current_gps_location: String? = null,
    val additional_notes: String? = null,
    val patient_username: String? = null
)

data class forgotPasswordRequest(
    val email: String
)
data class forgotPasswordResponse(
    val message: String? = null,
    val error : String? = null
)