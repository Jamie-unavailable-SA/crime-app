package com.example.crimewatch.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crimewatch.data.SessionManager
import com.example.crimewatch.data.models.*
import com.example.crimewatch.network.ApiService
import com.example.crimewatch.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService: ApiService = RetrofitInstance.apiService
    private val sessionManager = SessionManager(application)

    // ----------------------------
    // UI State
    // ----------------------------
    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations: StateFlow<List<Location>> = _locations

    private val _crimeTypes = MutableStateFlow<List<CrimeType>>(emptyList())
    val crimeTypes: StateFlow<List<CrimeType>> = _crimeTypes

    private val _riskLevels = MutableStateFlow<List<RiskLevel>>(emptyList())
    val riskLevels: StateFlow<List<RiskLevel>> = _riskLevels

    private val _reportCounts = MutableStateFlow<List<ReportCountSummary>>(emptyList())
    val reportCounts: StateFlow<List<ReportCountSummary>> = _reportCounts

    private val _recentReports = MutableStateFlow<List<RecentReport>>(emptyList())
    val recentReports: StateFlow<List<RecentReport>> = _recentReports

    private val TAG = "AnalyticsViewModel"

    init {
        fetchLocations()
        fetchCrimeTypes()
    }

    // ----------------------------
    // Fetch initial dropdown data
    // ----------------------------
    private fun fetchLocations() {
        viewModelScope.launch {
            try {
                val response = apiService.getLocations()
                if (response.isSuccessful) {
                    _locations.value = response.body() ?: emptyList()
                } else {
                    Log.e(TAG, "Failed to fetch locations: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching locations", e)
            }
        }
    }

    private fun fetchCrimeTypes() {
        viewModelScope.launch {
            try {
                val response = apiService.getCrimeTypes()
                if (response.isSuccessful) {
                    _crimeTypes.value = response.body() ?: emptyList()
                } else {
                    Log.e(TAG, "Failed to fetch crime types: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching crime types", e)
            }
        }
    }

    // ----------------------------
    // Fetch summary analytics
    // ----------------------------
    fun loadAnalytics(
        locationId: Int,
        crimeTypeId: Int? = null,
        range: String = "30 days"
    ) {
        viewModelScope.launch {
            try {
                val response = apiService.getAnalytics(
                    locationId = locationId,
                    crimeTypeId = crimeTypeId,
                    range = range
                )

                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        _riskLevels.value = data.risk_levels
                        _reportCounts.value = data.report_counts
                        _recentReports.value = data.recent_reports
                    }
                } else {
                    Log.e(TAG, "Analytics load failed: ${response.code()} ${response.message()}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading analytics", e)
            }
        }
    }
}