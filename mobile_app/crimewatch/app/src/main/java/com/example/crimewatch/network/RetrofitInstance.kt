package com.example.crimewatch.network

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // FastAPI backend URL for local dev (Android emulator: 10.0.2.2, physical device: use your PC's LAN IP)
    private const val BASE_URL = "http://192.168.100.5:8000/"

    fun createApiService(context: Context): ApiService {
        val okHttpClient = OkHttpClient.Builder()
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }

    val apiService: CrimeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)  // replace with your backend IP
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CrimeApiService::class.java)
    }
}