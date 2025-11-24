package com.example.crimewatch.network

import retrofit2.http.GET

data class CrimeType(
    val id: Int,
    val name: String,
    val description: String
)

data class Location(
    val id: Int,
    val area: String,
    val latitude: String,
    val longitude: String
)

interface CrimeApiService {
    @GET("api/crime-types")
    suspend fun getCrimeTypes(): List<CrimeType>

    @GET("api/locations")
    suspend fun getLocations(): List<Location>
}