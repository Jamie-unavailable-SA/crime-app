package com.example.crimewatch_mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.crimewatch_mobile.ui.screens.HeatmapScreen
import com.example.crimewatch_mobile.ui.screens.LandingScreen
import com.example.crimewatch_mobile.ui.screens.LoginScreen
import com.example.crimewatch_mobile.ui.screens.ProfileScreen
import com.example.crimewatch_mobile.ui.screens.RegisterScreen
import com.example.crimewatch_mobile.ui.screens.ReportScreen
import com.example.crimewatch_mobile.ui.screens.SettingsScreen


@Composable
fun AppGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "landing"
    ) {
        composable("landing") { LandingScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("report") { ReportScreen() }
        composable("heatmap") { HeatmapScreen() }
        composable("settings") { SettingsScreen() }
    }
}
