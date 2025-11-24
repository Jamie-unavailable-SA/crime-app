package com.example.crimewatch.network

import com.example.crimewatch.data.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import com.example.crimewatch.data.models.CrimeType
import com.example.crimewatch.data.models.Location

import retrofit2.http.*

interface ApiService {

    // AUTH
    @POST("api/reporters/login")
    suspend fun login(@Body payload: Map<String, String>): Response<LoginResponse>

    @POST("api/reporters/register")
    suspend fun register(@Body payload: Map<String, String?>): Response<RegisterResponse>

    @GET("api/reporters/{reporter_id}")
    suspend fun getProfile(@Path("reporter_id") reporterId: Int): Response<User>

    @PUT("api/reporters/{reporter_id}/update")
    suspend fun updateProfile(
        @Path("reporter_id") reporterId: Int,
        @Body request: UpdateProfileRequest
    ): Response<User>

    // REPORTS
    @FormUrlEncoded
    @POST("api/reports")
    suspend fun submitReport(
        @Field("reporter_id") reporterId: Int,
        @Field("crime_type_id") crimeTypeId: Int,
        @Field("location_id") locationId: Int,
        @Field("occurrence_time") occurrenceTime: String,
        @Field("description") description: String
    ): Response<ReportResponse>

    @Multipart
    @POST("api/reports/{report_id}/addons")
    suspend fun uploadReportAddon(
        @Path("report_id") reportId: Int,
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    // DROPDOWNS
    @GET("api/crime-types")
    suspend fun getCrimeTypes(): Response<List<CrimeType>>

    @GET("api/locations")
    suspend fun getLocations(): Response<List<Location>>

    @GET ("/api/analytics/location/{location_id}/crime-intensity")
    suspend fun getCrimeIntensity(
        @Path("location_id") locationId: Int
    ): Response<List<CrimeIntensity>>

    @GET("/api/analytics/location/{location_id}/crime-type/{crime_type_id}/trend")
    suspend fun getCrimeTrend(
        @Path("location_id") locationId: Int,
        @Path("crime_type_id") crimeTypeId: Int
    ): Response<List<CrimeTrendPoint>>

    @GET("/api/analytics/location/{location_id}/risk-levels")
    suspend fun getRiskLevels(
        @Path("location_id") locationId: Int
    ): Response<List<CrimeRisk>>




}