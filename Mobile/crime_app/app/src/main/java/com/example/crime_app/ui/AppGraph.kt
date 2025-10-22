package com.example.crime_app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.crime_app.ui.screens.LandingScreen
import com.example.crime_app.ui.screens.LoginScreen
import com.example.crime_app.ui.screens.RegisterScreen

@Composable
fun AppGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "landing"
    ) {
        composable("landing") { LandingScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
    }
}
