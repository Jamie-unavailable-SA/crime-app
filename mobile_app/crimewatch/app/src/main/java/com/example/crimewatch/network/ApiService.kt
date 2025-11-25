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


    @GET("api/analytics/summary")
    suspend fun getAnalyticsSummary(
        @Query("location_id") locationId: Int,
        @Query("crime_type_id") crimeTypeId: Int? = null,
        @Query("range") range: String = "30 days"
    ): List<ReportCountSummary>

    @GET("api/analytics/risk-levels")
    suspend fun getRiskLevels(
        @Query("location_id") locationId: Int
    ): List<RiskLevel>

    @GET("api/analytics/reports")
    suspend fun getRecentReports(
        @Query("location_id") locationId: Int,
        @Query("limit") limit: Int = 10
    ): List<RecentReport>

    @GET("api/analytics")
    suspend fun getAnalytics(
        @Query("location_id") locationId: Int,
        @Query("crime_type_id") crimeTypeId: Int? = null,
        @Query("range") range: String = "30 days"
    ): Response<AnalyticsResponse>

    @DELETE("api/reporters/{reporter_id}")
    suspend fun deleteAccount(
        @Path("reporter_id") reporterId: Int,
        @Query("confirm") confirm: Boolean = true
    ): Response<Unit>
}