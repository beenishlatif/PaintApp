package com.example.paintingapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // Splash XML

        // Status bar color optional
        window.statusBarColor = resources.getColor(R.color.purple_bg, theme)

        // 2 second baad OnboardingActivity open ho
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish() // taki back button se wapas splash na aaye
        }, 2000) // 2000ms = 2 seconds
    }
}
