package com.example.crimewatch_mobile.data.models

data class RegisterRequest(
	val alias: String,
	val password: String,
	val email: String? = null,
	val phone: String? = null
)