package com.example.crimewatch_mobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crimewatch_mobile.data.SessionManager
import com.example.crimewatch_mobile.data.models.LoginRequest
import com.example.crimewatch_mobile.data.models.LoginResponse
import com.example.crimewatch_mobile.data.models.RegisterRequest
import com.example.crimewatch_mobile.data.models.RegisterResponse
import com.example.crimewatch_mobile.data.models.UpdateProfileRequest
import com.example.crimewatch_mobile.data.models.User
import com.example.crimewatch_mobile.network.ApiService
import com.example.crimewatch_mobile.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val apiService: ApiService = RetrofitInstance.createApiService(application)

    private val _loginState = MutableStateFlow<LoginResult>(LoginResult.Empty)
    val loginState: StateFlow<LoginResult> = _loginState

    private val _registrationState = MutableStateFlow<RegistrationResult>(RegistrationResult.Empty)
    val registrationState: StateFlow<RegistrationResult> = _registrationState

    private val _userProfileState = MutableStateFlow<UserProfileResult>(UserProfileResult.Empty)
    val userProfileState: StateFlow<UserProfileResult> = _userProfileState

    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginResult.Loading
            try {
                val response = apiService.login(LoginRequest(identifier, password))
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.accessToken?.let { sessionManager.saveAuthToken(it) }
                    _loginState.value = LoginResult.Success(response.body()!!)
                } else {
                    _loginState.value = LoginResult.Error("Login failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _loginState.value = LoginResult.Error(e.message ?: "An error occurred during login")
            }
        }
    }

    fun register(alias: String, password: String, email: String? = null, phone: String? = null) {
        viewModelScope.launch {
            _registrationState.value = RegistrationResult.Loading
            try {
                val response = apiService.register(RegisterRequest(alias, password, email, phone))
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
                val response = apiService.getProfile()
                if (response.isSuccessful && response.body() != null) {
                    _userProfileState.value = UserProfileResult.Success(response.body()!!)
                } else {
                    _userProfileState.value = UserProfileResult.Error("Failed to fetch profile: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _userProfileState.value = UserProfileResult.Error(e.message ?: "An error occurred during profile fetch")
            }
        }
    }

    fun updateProfile(alias: String, firstName: String?, lastName: String?, email: String?, phone: String?) {
        viewModelScope.launch {
            _userProfileState.value = UserProfileResult.Loading
            try {
                val response = apiService.updateProfile(UpdateProfileRequest(alias, firstName, lastName, email, phone))
                if (response.isSuccessful && response.body() != null) {
                    _userProfileState.value = UserProfileResult.Success(response.body()!!)
                } else {
                    _userProfileState.value = UserProfileResult.Error("Failed to update profile: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _userProfileState.value = UserProfileResult.Error(e.message ?: "An error occurred during profile update")
            }
        }
    }

    fun logout() {
        sessionManager.clearAuthToken()
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
