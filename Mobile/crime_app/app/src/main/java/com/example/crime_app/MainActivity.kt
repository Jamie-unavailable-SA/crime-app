package com.example.crime_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.crime_app.ui.AppGraph
import com.example.crime_app.ui.theme.Crime_appTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Crime_appTheme {
                val navController = rememberNavController()
                AppGraph(navController = navController)
            }
        }
    }
}
