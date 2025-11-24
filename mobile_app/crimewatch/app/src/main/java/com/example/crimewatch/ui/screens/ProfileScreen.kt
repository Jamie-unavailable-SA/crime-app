package com.example.crimewatch.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.crimewatch.viewmodel.AuthViewModel
import com.example.crimewatch.viewmodel.UserProfileResult

@Composable
fun ProfileScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    val userProfileState by authViewModel.userProfileState.collectAsState()
    var alias by remember { mutableStateOf("") }
    var f_name by remember { mutableStateOf("") }
    var l_name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    // Safely update state when the user profile is loaded
    LaunchedEffect(userProfileState) {
        if (userProfileState is UserProfileResult.Success) {
            val user = (userProfileState as UserProfileResult.Success).user
            alias = user.alias
            f_name = user.f_name ?: ""
            l_name = user.l_name ?: ""
            email = user.email ?: ""
            phone = user.phone ?: ""
        }
    }
    // Fetch the profile when the screen is first composed
    LaunchedEffect(Unit) {
        authViewModel.getProfile()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "My Profile",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(20.dp))
        // Handle and display the UI for each state
        when (userProfileState) {
            is UserProfileResult.Loading -> {
                CircularProgressIndicator()
            }
            is UserProfileResult.Error -> {
                Text(text = (userProfileState as UserProfileResult.Error).message, color = Color.Red)
            }
            is UserProfileResult.Success -> {
                ProfileForm(
                    alias = alias, onAliasChange = { alias = it },
                    firstName = f_name, onFirstNameChange = { f_name = it },
                    lastName = l_name, onLastNameChange = { l_name = it },
                    email = email, onEmailChange = { email = it },
                    phone = phone, onPhoneChange = { phone = it },
                    onSaveChanges = {
                        authViewModel.updateProfile(alias, f_name, l_name, email, phone)
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("landing") { popUpTo(0) } // Clear back stack
                    }
                )
            }
            is UserProfileResult.Empty -> {
                // You can show a placeholder, a message, or nothing here
            }
        }
    }
}

// A stateless component for the profile form
@Composable
private fun ProfileForm(
    alias: String, onAliasChange: (String) -> Unit,
    firstName: String, onFirstNameChange: (String) -> Unit,
    lastName: String, onLastNameChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    onSaveChanges: () -> Unit,
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = alias, onValueChange = onAliasChange, label = { Text("Alias") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = firstName, onValueChange = onFirstNameChange, label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = lastName, onValueChange = onLastNameChange, label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = email, onValueChange = onEmailChange, label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = phone, onValueChange = onPhoneChange, label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(25.dp))
        Button(
            onClick = onSaveChanges,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Text("Save Changes")
        }
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(onClick = onLogout) {
            Text("Log Out", color = MaterialTheme.colorScheme.error)
        }
    }
}
