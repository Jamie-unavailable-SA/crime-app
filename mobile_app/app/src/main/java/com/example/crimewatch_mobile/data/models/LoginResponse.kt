package com.example.crimewatch_mobile.data.models

import com.google.gson.annotations.SerializedName

// Mobile API returns a status, reporter_id, and access_token for reporter login
data class LoginResponse(
    val status: String,
    val reporter_id: Int,
    @SerializedName("access_token") // This ensures it correctly parses the field from the JSON
    val accessToken: String
)
