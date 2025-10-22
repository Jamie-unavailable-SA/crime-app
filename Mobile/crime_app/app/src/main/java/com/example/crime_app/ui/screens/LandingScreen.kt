package com.example.crime_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun LandingScreen(navController: NavController) {
    // 🎨 Color Scheme (consistent app-wide)
    val backgroundColor = Color(0xFFF6F8FA)   // light background
    val textColor = Color(0xFF24292F)         // main text color
    val redButton = Color(0xFFE5534B)         // for DB interaction buttons
    val darkButton = Color(0xFF24292F)        // for navigation/general actions

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Title
            Text(
                text = "CrimeWatch",
                fontSize = 34.sp,
                color = textColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Stay informed. Report incidents. Improve safety.",
                color = textColor.copy(alpha = 0.8f),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 🔴 Database interaction button (Register)
            Button(
                onClick = { navController.navigate("register") },
                colors = ButtonDefaults.buttonColors(containerColor = darkButton),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Register", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ⚫ General navigation button (Login screen)
            Button(
                onClick = { navController.navigate("login") },
                colors = ButtonDefaults.buttonColors(containerColor = darkButton),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Login", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
