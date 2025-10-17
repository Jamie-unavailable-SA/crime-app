package com.example.crimewatch

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.crimewatch.ui.theme.CrimeWatchTheme
import com.example.crimewatch.ui.AppUI
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        //testFirestore()

        setContent {
            CrimeWatchTheme {
                AppUI() // or AppNavHost() later when navigation is ready
            }
        }
    }

    private fun testFirestore() {
        val db = FirebaseFirestore.getInstance()
        val testData = hashMapOf(
            "name" to "Firebase Test",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("testCollection")
            .add(testData)
            .addOnSuccessListener { documentRef ->
                Log.d("FirestoreTest", "✅ Document added with ID: ${documentRef.id}")
                documentRef.get().addOnSuccessListener { document ->
                    Log.d("FirestoreTest", "Document data: ${document.data}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreTest", "❌ Error adding document", e)
            }
    }
}
