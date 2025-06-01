package com.yousef.mysight00.model


data class EditProfileRequest(
    val username: String,
    val email: String,
    val phone_number: String,
    val profile_photo: String? = null,
    val related_companion: RelatedCompanion? = null
)

data class RelatedCompanion(
    val username: String,
    val relationship: String,
    val companion_user: String,
    val patient_user: String
)

data class EditProfileResponse(
    val id: Int,
    val username: String,
    val email: String,
    val phone_number: String,
    val name: String,
    val profile_photo: String?,
    val related_companion: RelatedCompanion?
)