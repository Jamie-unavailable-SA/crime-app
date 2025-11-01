package com.example.crimewatch_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crimewatch_mobile.data.models.LoginRequest
import com.example.crimewatch_mobile.data.models.LoginResponse
import com.example.crimewatch_mobile.data.models.RegisterRequest
import com.example.crimewatch_mobile.data.models.RegisterResponse
import com.example.crimewatch_mobile.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _loginState = MutableStateFlow<LoginResult>(LoginResult.Empty)
    val loginState: StateFlow<LoginResult> = _loginState

    private val _registrationState = MutableStateFlow<RegistrationResult>(RegistrationResult.Empty)
    val registrationState: StateFlow<RegistrationResult> = _registrationState

    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginResult.Loading
            try {
                val response = RetrofitInstance.api.login(LoginRequest(identifier, password))
                if (response.isSuccessful && response.body() != null) {
                    _loginState.value = LoginResult.Success(response.body()!!)
                } else {
                    _loginState.value = LoginResult.Error("Login failed")
                }
            } catch (e: Exception) {
                _loginState.value = LoginResult.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun register(alias: String, password: String, email: String? = null, phone: String? = null) {
        viewModelScope.launch {
            _registrationState.value = RegistrationResult.Loading
            try {
                val response = RetrofitInstance.api.register(RegisterRequest(alias, password, email, phone))
                if (response.isSuccessful && response.body() != null) {
                    _registrationState.value = RegistrationResult.Success(response.body()!!)
                } else {
                    _registrationState.value = RegistrationResult.Error("Registration failed")
                }
            } catch (e: Exception) {
                _registrationState.value = RegistrationResult.Error(e.message ?: "An error occurred")
            }
        }
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
