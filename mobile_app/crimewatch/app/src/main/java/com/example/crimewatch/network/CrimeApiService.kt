package com.example.crimewatch.network

import retrofit2.http.GET

data class CrimeType(
    val id: Int,
    val name: String,
    val description: String?
)

interface CrimeTypeApiService {
    @GET("api/crime-types")
    suspend fun getCrimeTypes(): List<CrimeType>
}