package com.example.crimewatch.data.models

import com.google.gson.annotations.SerializedName

data class ReportRequest(
    @SerializedName("reporter_id") val reporterId: Int,
    @SerializedName("crime_type_id") val crimeTypeId: Int,
    val description: String,
    @SerializedName("area_id") val areaId: Int,
    @SerializedName("report_type") val reportType: String,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("image_urls") val imageUrls: List<String>?,
    @SerializedName("occurrence_time") val occurrenceTime: String?
)
