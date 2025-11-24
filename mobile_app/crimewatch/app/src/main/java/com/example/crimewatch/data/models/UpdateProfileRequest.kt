package com.example.crimewatch.data.models

data class UpdateProfileRequest(
    val alias: String,
    val f_name: String?,
    val l_name: String?,
    val email: String?,
    val phone: String?
)