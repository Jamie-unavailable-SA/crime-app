package com.example.crimewatch.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
    object Report : BottomNavItem("report", Icons.Default.Edit, "Report")
    object Analytics : BottomNavItem("analytics", Icons.Default.Place, "Analytics")
    object Settings : BottomNavItem("settings", Icons.Default.Settings, "Settings")
}