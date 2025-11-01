package com.example.crimewatch_mobile.data.models

// Mobile API returns a simple status and reporter_id for reporter login
data class LoginResponse(val status: String, val reporter_id: Int)