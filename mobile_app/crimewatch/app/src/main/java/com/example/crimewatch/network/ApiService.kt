package com.example.crimewatch.network

import com.example.crimewatch.data.models.LoginRequest
import com.example.crimewatch.data.models.LoginResponse
import com.example.crimewatch.data.models.RegisterRequest
import com.example.crimewatch.data.models.RegisterResponse
import com.example.crimewatch.data.models.UpdateProfileRequest
import com.example.crimewatch.data.models.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // Mobile reporter endpoints
    @POST("api/reporters/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/reporters/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("api/reporters/{reporter_id}")
    suspend fun getProfile(@Path("reporter_id") reporterId: Int): Response<User>

    @PUT("api/reporters/{reporter_id}/update")
    suspend fun updateProfile(
        @Path("reporter_id") reporterId: Int,
        @Body request: UpdateProfileRequest
    ): Response<User>
}
