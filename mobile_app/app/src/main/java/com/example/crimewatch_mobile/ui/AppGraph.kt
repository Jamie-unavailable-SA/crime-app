package com.example.crimewatch_mobile.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.crimewatch_mobile.ui.screens.LandingScreen
import com.example.crimewatch_mobile.ui.screens.LoginScreen
import com.example.crimewatch_mobile.ui.screens.RegisterScreen


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
