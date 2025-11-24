package com.example.crimewatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.crimewatch.ui.navigation.AppGraph
import com.example.crimewatch.ui.theme.CrimewatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CrimewatchTheme {
                val navController = rememberNavController()
                AppGraph(navController = navController)
            }
        }
    }
}
