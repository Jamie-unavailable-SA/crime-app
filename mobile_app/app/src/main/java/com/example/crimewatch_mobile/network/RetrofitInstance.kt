package com.example.crimewatch_mobile.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // FastAPI backend URL for local dev (Android emulator: 10.0.2.2, physical device: use your PC's LAN IP)
    private const val BASE_URL = "http://192.168.100.5:8000/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
