package com.example.crimewatch.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crimewatch.data.SessionManager
import com.example.crimewatch.network.ApiService
import com.example.crimewatch.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private val Application.dataStore by preferencesDataStore("settings")

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.dataStore
    private val sessionManager = SessionManager(application)
    private val apiService: ApiService = RetrofitInstance.apiService

    private val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications")
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

    private val _notifications = MutableStateFlow(true)
    val notifications: StateFlow<Boolean> = _notifications

    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                _notifications.value = prefs[NOTIFICATIONS_KEY] ?: true
                _darkMode.value = prefs[DARK_MODE_KEY] ?: false
            }
        }
    }

    fun setNotifications(enabled: Boolean) {
        _notifications.value = enabled
        saveSettings()
    }

    fun setDarkMode(enabled: Boolean) {
        _darkMode.value = enabled
        saveSettings()
    }

    private fun saveSettings() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[NOTIFICATIONS_KEY] = _notifications.value
                prefs[DARK_MODE_KEY] = _darkMode.value
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val reporterId = sessionManager.getReporterId()

                if (reporterId == -1) {
                    onError("User not logged in")
                    return@launch
                }

                val response = apiService.deleteAccount(
                    reporterId = reporterId,
                    confirm = true
                )

                if (response.isSuccessful) {
                    // Clear local saved session
                    sessionManager.clearSession()
                    onSuccess()
                } else {
                    onError("Failed: ${response.code()} ${response.message()}")
                }

            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }
}
