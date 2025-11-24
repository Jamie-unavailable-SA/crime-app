package com.example.crimewatch.data.models

import com.google.gson.annotations.SerializedName

// Mobile API returns a status, and reporter_id for reporter login
data class LoginResponse(
    val status: String,
    @field:SerializedName("reporter_id")
    val reporterId: Int
)
