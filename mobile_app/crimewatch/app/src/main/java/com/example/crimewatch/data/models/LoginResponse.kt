package com.example.crimewatch.data.models

import com.google.gson.annotations.SerializedName

data class LoginResponse(

    val status: String? = null,

    @SerializedName("reporter_id")
    val reporterId: Int? = null,

    val alias: String? = null,

    @SerializedName("f_name")
    val fName: String? = null,

    @SerializedName("l_name")
    val lName: String? = null,

    val email: String? = null,
    val phone: String? = null,

    @SerializedName("date_joined")
    val dateJoined: String? = null,

    @SerializedName("last_login")
    val lastLogin: String? = null,

    // backend may return {"error": "..."}
    val error: String? = null
)
