package com.example.crimewatch.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("CrimeWatchPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val REPORTER_ID = "reporter_id"
    }

    fun saveSession(reporterId: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(REPORTER_ID, reporterId)
        editor.apply()
    }

    fun getReporterId(): Int {
        return sharedPreferences.getInt(REPORTER_ID, -1)
    }

    fun clearSession() {
        val editor = sharedPreferences.edit()
        editor.remove(REPORTER_ID)
        editor.apply()
    }
}