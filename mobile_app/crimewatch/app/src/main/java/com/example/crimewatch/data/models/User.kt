package com.example.crimewatch.data.models
data class User(
    val id: Int,
    val alias: String,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phone: String?
)