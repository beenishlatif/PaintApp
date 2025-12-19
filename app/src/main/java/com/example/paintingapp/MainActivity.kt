package com.example.paintingapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Splash screen layout
        setContentView(R.layout.activity_splash)

        // 2 sec baad LoginSignupActivity open
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginSignupActivity::class.java))
            finish()
        }, 2000)
    }
}
