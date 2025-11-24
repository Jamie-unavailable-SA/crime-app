package com.example.crimewatch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crimewatch.data.models.CrimeIntensity
import com.example.crimewatch.data.models.CrimeRisk
import com.example.crimewatch.data.models.CrimeTrendPoint
import com.example.crimewatch.data.models.CrimeType
import com.example.crimewatch.data.models.Location
import com.example.crimewatch.network.ApiService
import com.example.crimewatch.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val api: ApiService = RetrofitInstance.apiService

    private val _crimeIntensity = MutableStateFlow<List<CrimeIntensity>>(emptyList())
    val crimeIntensity = _crimeIntensity.asStateFlow()

    private val _crimeTrend = MutableStateFlow<List<CrimeTrendPoint>>(emptyList())
    val crimeTrend = _crimeTrend.asStateFlow()

    private val _riskLevels = MutableStateFlow<List<CrimeRisk>>(emptyList())
    val riskLevels = _riskLevels.asStateFlow()

    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations = _locations.asStateFlow()

    private val _crimeTypes = MutableStateFlow<List<CrimeType>>(emptyList())
    val crimeTypes = _crimeTypes.asStateFlow()

    init {
        viewModelScope.launch {
            _locations.value = api.getLocations().body() ?: emptyList()
            _crimeTypes.value = api.getCrimeTypes().body() ?: emptyList()
        }
    }

    fun loadAnalytics(locationId: Int, crimeTypeId: Int?) {
        viewModelScope.launch {

            // Load bar chart data
            _crimeIntensity.value =
                api.getCrimeIntensity(locationId).body() ?: emptyList()

            // Load line chart (only when crime is selected)
            if (crimeTypeId != null) {
                _crimeTrend.value =
                    api.getCrimeTrend(locationId, crimeTypeId).body() ?: emptyList()
            } else {
                _crimeTrend.value = emptyList()
            }

            // Load risk levels
            _riskLevels.value =
                api.getRiskLevels(locationId).body() ?: emptyList()
        }
    }
}
