package com.example.crimewatch_mobile.network

import com.example.crimewatch_mobile.data.models.LoginRequest
import com.example.crimewatch_mobile.data.models.LoginResponse
import com.example.crimewatch_mobile.data.models.RegisterRequest
import com.example.crimewatch_mobile.data.models.RegisterResponse
import com.example.crimewatch_mobile.data.models.UpdateProfileRequest
import com.example.crimewatch_mobile.data.models.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {

    // Mobile reporter endpoints
    @POST("api/reporters/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/reporters/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("api/reporters/me")
    suspend fun getProfile(): Response<User> // Assuming this is the endpoint to get the user's profile

    @PUT("api/reporters/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<User> // Assuming this is the endpoint to update the user's profile
}
