package com.example.paintingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var btnSkip: TextView
    private lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        btnSkip = findViewById(R.id.btnSkip)
        btnNext = findViewById(R.id.btnNext)

        // 🔹 Skip → SplashActivity
        btnSkip.setOnClickListener {
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 🔹 Next → LoginSignupActivity
        btnNext.setOnClickListener {
            val intent = Intent(this, LoginSignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
