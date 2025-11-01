package com.example.crimewatch_mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.crimewatch_mobile.viewmodel.AuthViewModel
import com.example.crimewatch_mobile.viewmodel.RegistrationResult


@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var alias by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val registrationState by authViewModel.registrationState.collectAsState()
    var passwordMismatchError by remember { mutableStateOf(false) }

    val redColor = Color(0xFFE5534B)
    val blackColor = Color(0xFF24292F)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = blackColor
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = alias,
            onValueChange = { alias = it },
            label = { Text("Alias (username)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordMismatchError,
            modifier = Modifier.fillMaxWidth()
        )
        if (passwordMismatchError) {
            Text(text = "Passwords do not match", color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (registrationState) {
            is RegistrationResult.Loading -> {
                CircularProgressIndicator()
            }
            is RegistrationResult.Error -> {
                Text(text = (registrationState as RegistrationResult.Error).message, color = Color.Red)
            }
            is RegistrationResult.Success -> {
                navController.navigate("login")
            }
            else -> {}
        }

        Button(
            onClick = {
                if (password == confirmPassword) {
                    passwordMismatchError = false
                    authViewModel.register(alias, password, email.ifBlank { null }, phone.ifBlank { null })
                } else {
                    passwordMismatchError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = redColor,
                contentColor = Color.White
            )
        ) {
            Text("Register", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account?",
                color = blackColor
            )
            TextButton(onClick = { navController.navigate("login") }) {
                Text(
                    text = " Login here",
                    color = blackColor,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}
