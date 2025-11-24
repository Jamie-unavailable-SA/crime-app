package com.example.crimewatch.network

import android.content.Context
import com.example.crimewatch.data.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {

    private val sessionManager = SessionManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        val reporterId = sessionManager.getReporterId()
        if (reporterId != -1) {
            requestBuilder.addHeader("X-Reporter-Id", reporterId.toString())
        }

        return chain.proceed(requestBuilder.build())
    }
}