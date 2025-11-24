package com.example.crimewatch.data.models

import com.google.gson.annotations.SerializedName

data class ReportRequest(
    @SerializedName("reporter_id") val reporterId: Int,
    @SerializedName("crime_type_id") val crimeTypeId: Int,
    @SerializedName("location_id") val locationId: Int,
    val description: String,
    @SerializedName("occurrence_time") val occurrenceTime: String
)
