package com.example.crimewatch_mobile.data.models

data class UpdateProfileRequest(
    val alias: String,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phone: String?
)
