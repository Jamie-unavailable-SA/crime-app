package com.example.crimewatch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crimewatch.data.SessionManager
import com.example.crimewatch.data.models.LoginResponse
import com.example.crimewatch.data.models.RegisterResponse
import com.example.crimewatch.data.models.UpdateProfileRequest
import com.example.crimewatch.data.models.User
import com.example.crimewatch.network.ApiService
import com.example.crimewatch.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val apiService: ApiService = RetrofitInstance.apiService

    private val _loginState = MutableStateFlow<LoginResult>(LoginResult.Empty)
    val loginState: StateFlow<LoginResult> = _loginState

    private val _registrationState = MutableStateFlow<RegistrationResult>(RegistrationResult.Empty)
    val registrationState: StateFlow<RegistrationResult> = _registrationState

    private val _userProfileState = MutableStateFlow<UserProfileResult>(UserProfileResult.Empty)
    val userProfileState: StateFlow<UserProfileResult> = _userProfileState

    fun login(identifier: String, password: String) {
        viewModelScope.launch {

            // Prevent empty input crash
            if (identifier.isBlank() || password.isBlank()) {
                _loginState.value = LoginResult.Error("Please fill in all fields.")
                return@launch
            }

            _loginState.value = LoginResult.Loading

            try {
                val payload = mapOf("identifier" to identifier, "password" to password)
                val response = apiService.login(payload)

                // If backend returns HTTP error (400, 422, etc)
                if (!response.isSuccessful) {
                    _loginState.value = LoginResult.Error("Invalid username or password.")
                    return@launch
                }

                val body = response.body()

                // If backend returns "error" OR missing status
                if (body == null || body.error != null || body.status != "ok") {
                    _loginState.value = LoginResult.Error("Invalid username or password.")
                    return@launch
                }

                // Save session safely
                sessionManager.saveSession(body.reporterId ?: -1)

                _loginState.value = LoginResult.Success(body)

            } catch (e: Exception) {
                _loginState.value = LoginResult.Error("Network error: ${e.message}")
            }
        }
    }


    fun register(alias: String, password: String, email: String? = null, phone: String? = null) {
        viewModelScope.launch {
            _registrationState.value = RegistrationResult.Loading
            try {
                val payload = mutableMapOf<String, String?>(
                    "alias" to alias,
                    "password" to password
                )
                email?.let { payload["email"] = it }
                phone?.let { payload["phone"] = it }


                val response = apiService.register(payload)
                if (response.isSuccessful && response.body() != null) {
                    _registrationState.value = RegistrationResult.Success(response.body()!!)
                } else {
                    _registrationState.value = RegistrationResult.Error("Registration failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _registrationState.value = RegistrationResult.Error(e.message ?: "An error occurred during registration")
            }
        }
    }

    fun getProfile() {
        viewModelScope.launch {
            _userProfileState.value = UserProfileResult.Loading
            try {
                val reporterId = sessionManager.getReporterId()
                if (reporterId != -1) {
                    val response = apiService.getProfile(reporterId)
                    if (response.isSuccessful && response.body() != null) {
                        _userProfileState.value = UserProfileResult.Success(response.body()!!)
                    } else {
                        _userProfileState.value = UserProfileResult.Error("Failed to fetch profile: ${response.code()} ${response.message()}")
                    }
                } else {
                    _userProfileState.value = UserProfileResult.Error("User not logged in")
                }
            } catch (e: Exception) {
                _userProfileState.value = UserProfileResult.Error(e.message ?: "An error occurred during profile fetch")
            }
        }
    }

    fun updateProfile(alias: String, f_name: String?, l_name: String?, email: String?, phone: String?) {
        viewModelScope.launch {
            _userProfileState.value = UserProfileResult.Loading
            try {
                val reporterId = sessionManager.getReporterId()
                if (reporterId != -1) {
                    val response = apiService.updateProfile(reporterId, UpdateProfileRequest(alias, f_name, l_name, email, phone))
                    if (response.isSuccessful && response.body() != null) {
                        _userProfileState.value = UserProfileResult.Success(response.body()!!)
                    } else {
                        _userProfileState.value = UserProfileResult.Error("Failed to update profile: ${response.code()} ${response.message()}")
                    }
                } else {
                    _userProfileState.value = UserProfileResult.Error("User not logged in")
                }
            } catch (e: Exception) {
                _userProfileState.value = UserProfileResult.Error(e.message ?: "An error occurred during profile update")
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _loginState.value = LoginResult.Empty
        _userProfileState.value = UserProfileResult.Empty
    }
}

sealed class LoginResult {
    object Empty : LoginResult()
    object Loading : LoginResult()
    data class Success(val loginResponse: LoginResponse) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

sealed class RegistrationResult {
    object Empty : RegistrationResult()
    object Loading : RegistrationResult()
    data class Success(val response: RegisterResponse) : RegistrationResult()
    data class Error(val message: String) : RegistrationResult()
}

sealed class UserProfileResult {
    object Empty : UserProfileResult()
    object Loading : UserProfileResult()
    data class Success(val user: User) : UserProfileResult()
    data class Error(val message: String) : UserProfileResult()
}