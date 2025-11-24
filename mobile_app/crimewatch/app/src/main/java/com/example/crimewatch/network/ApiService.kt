package com.example.crimewatch.network

import com.example.crimewatch.data.models.CrimeType
import com.example.crimewatch.data.models.Location
import com.example.crimewatch.data.models.LoginResponse
import com.example.crimewatch.data.models.RegisterResponse
import com.example.crimewatch.data.models.ReportResponse
import com.example.crimewatch.data.models.UpdateProfileRequest
import com.example.crimewatch.data.models.UploadResponse
import com.example.crimewatch.data.models.User
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    // Mobile reporter endpoints
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

    @FormUrlEncoded
    @POST("api/reports")
    suspend fun submitReport(
        @Field("reporter_id") reporterId: Int,
        @Field("crime_type_id") crimeTypeId: Int,
        @Field("location_id") locationId: Int,
        @Field("occurrence_time") occurrenceTime: String,
        @Field("description") description: String
    ): Response<ReportResponse>

    // Endpoints for crime types and locations
    @GET("api/crime-types")
    suspend fun getCrimeTypes(): Response<List<CrimeType>>

    @GET("api/locations")
    suspend fun getLocations(): Response<List<Location>>

    @Multipart
    @POST("api/reports/{report_id}/addons")
    suspend fun uploadReportAddon(
        @Path("report_id") reportId: Int,
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>
}