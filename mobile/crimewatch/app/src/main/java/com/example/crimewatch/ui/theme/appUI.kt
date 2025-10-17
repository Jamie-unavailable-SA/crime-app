package com.example.crimewatch.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.compose.rememberNavController
import com.example.crimewatch.AppNavGraph
import com.example.crimewatch.ui.theme.CrimeWatchTheme

@Composable
fun AppUI() {
    CrimeWatchTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val navController = rememberNavController()
            AppNavGraph(navController)
        }
    }
}
