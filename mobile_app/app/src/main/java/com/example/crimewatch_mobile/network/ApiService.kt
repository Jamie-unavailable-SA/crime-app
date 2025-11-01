package com.example.crimewatch_mobile.network

import com.example.crimewatch_mobile.data.models.LoginRequest
import com.example.crimewatch_mobile.data.models.LoginResponse
import com.example.crimewatch_mobile.data.models.RegisterRequest
import com.example.crimewatch_mobile.data.models.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    // Mobile reporter endpoints
    @POST("api/reporters/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/reporters/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
}
