package com.example.crimewatch.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.crimewatch.ui.screens.AnalyticsScreen
import com.example.crimewatch.ui.screens.LandingScreen
import com.example.crimewatch.ui.screens.LoginScreen
import com.example.crimewatch.ui.screens.ProfileScreen
import com.example.crimewatch.ui.screens.RegisterScreen
import com.example.crimewatch.ui.screens.ReportScreen
import com.example.crimewatch.ui.screens.SettingsScreen
import androidx.compose.ui.Modifier


@Composable
fun AppGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "landing",
        modifier = Modifier
    ) {
        composable("landing") { LandingScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("report") { ReportScreen() }
        composable("analytics") { AnalyticsScreen() }
        composable("settings") { SettingsScreen() }
    }
}
