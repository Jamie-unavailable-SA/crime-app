package com.example.crimewatch.data.models

import com.google.gson.annotations.SerializedName

data class ReportResponse(
    val status: String,
    @SerializedName("report_id") val reportId: Int
)