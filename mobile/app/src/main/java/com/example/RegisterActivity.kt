package com.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            // TODO: handle register logic
            finish() // return to login after registering
        }
    }
}
