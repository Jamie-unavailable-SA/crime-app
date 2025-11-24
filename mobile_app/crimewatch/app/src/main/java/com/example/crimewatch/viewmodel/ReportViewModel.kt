package com.example.crimewatch.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crimewatch.data.SessionManager
import com.example.crimewatch.data.models.CrimeType
import com.example.crimewatch.data.models.Location
import com.example.crimewatch.data.models.ReportResponse
import com.example.crimewatch.network.ApiService
import com.example.crimewatch.network.RetrofitInstance
import com.example.crimewatch.util.FileUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class ReportViewModel(application: Application) : AndroidViewModel(application) {

    // Use unified API service
    private val apiService: ApiService = RetrofitInstance.apiService
    private val sessionManager = SessionManager(application)

    private val context: Context
        get() = getApplication<Application>().applicationContext

    private val _reportState = MutableStateFlow<ReportState>(ReportState.Empty)
    val reportState: StateFlow<ReportState> = _reportState.asStateFlow()

    private val _crimeTypes = MutableStateFlow<List<CrimeType>>(emptyList())
    val crimeTypes: StateFlow<List<CrimeType>> = _crimeTypes.asStateFlow()

    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations: StateFlow<List<Location>> = _locations.asStateFlow()

    init {
        fetchCrimeTypes()
        fetchLocations()
    }

    private fun fetchCrimeTypes() {
        viewModelScope.launch {
            try {
                val response = apiService.getCrimeTypes()
                if (response.isSuccessful) {
                    response.body()?.let {
                        _crimeTypes.value = it
                    }
                } else {
                    Log.e("ReportViewModel", "Failed to fetch crime types: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Exception while fetching crime types", e)
            }
        }
    }

    private fun fetchLocations() {
        viewModelScope.launch {
            try {
                val response = apiService.getLocations()
                if (response.isSuccessful) {
                    response.body()?.let {
                        _locations.value = it
                    }
                } else {
                    Log.e("ReportViewModel", "Failed to fetch locations: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "Exception while fetching locations", e)
            }
        }
    }

    private suspend fun uploadFiles(reportId: Int, uris: List<Uri>) {
        uris.map { uri ->
            viewModelScope.async {
                try {
                    val file = FileUtil.getFileFromUri(context, uri)
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                    apiService.uploadReportAddon(reportId, body)
                } catch (e: Exception) {
                    Log.e("ReportViewModel", "Exception while uploading file", e)
                }
            }
        }.awaitAll()
    }

    fun submitReport(
        crimeTypeId: Int?,
        description: String,
        locationId: Int?,
        occurrenceTime: String?,
        imageUris: List<Uri>
    ) {
        viewModelScope.launch {
            _reportState.value = ReportState.Loading

            val reporterId = sessionManager.getReporterId()
            if (reporterId == -1) {
                _reportState.value = ReportState.Error("User not logged in.")
                return@launch
            }

            if (crimeTypeId == null || locationId == null || occurrenceTime == null) {
                _reportState.value = ReportState.Error("All fields are required.")
                return@launch
            }

            try {
                val response = apiService.submitReport(
                    reporterId = reporterId,
                    crimeTypeId = crimeTypeId,
                    locationId = locationId,
                    description = description,
                    occurrenceTime = occurrenceTime
                )

                if (response.isSuccessful) {
                    val reportId = response.body()?.reportId
                    if (reportId != null && imageUris.isNotEmpty()) {
                        uploadFiles(reportId, imageUris)
                    }
                    _reportState.value = ReportState.Success(response.body()!!)
                } else {
                    _reportState.value = ReportState.Error(
                        "Failed to submit report: ${response.code()} - ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                _reportState.value = ReportState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    sealed class ReportState {
        object Empty : ReportState()
        object Loading : ReportState()
        data class Success(val response: ReportResponse) : ReportState()
        data class Error(val message: String) : ReportState()
    }
}
