package com.yousef.mysight00.model

data class loginRequest(
    val email: String,
    val password: String
)

data class loginResponse(
    val access: String? = null,
    val refresh: String? = null,
    val user: User? = null,
    val detail: String? = null
)

data class User(
    val id: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val account_type: String? = null,
    val phone_number: String? = null,
    val location: String? = null,
    val username: String? = null,
    val patient_username: String? = null,
    val linked_patient_name: String? = null,
    val account_photo: String? = null,
    val current_gps_location: String? = null,
    val additional_notes: String? = null,
    val medical_condition: String? = null,
    val relationship: String? = null,
    val linked_patient_type: String? = null,
    val linked_companion_name: String? = null
)

data class forgotPasswordRequest(
    val email: String
)

data class forgotPasswordResponse(
    val message: String? = null,
    val error: String? = null
)