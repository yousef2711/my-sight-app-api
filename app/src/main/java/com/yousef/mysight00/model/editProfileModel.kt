package com.yousef.mysight00.model


data class EditProfileRequest(
    val username: String,
    val phone_number: String,
    val name: String,
    val profile_photo: String? = null
)

data class EditProfileResponse(
    val id: Int,
    val username: String,
    val phone_number: String,
    val name: String,
    val profile_photo: String?
)